package com.pepej.gungame.listener;

import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import com.pepej.papi.utils.Players;
import net.luckperms.api.LuckPerms;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.pepej.papi.events.Events.merge;
import static com.pepej.papi.events.Events.subscribe;
import static com.pepej.papi.text.Text.colorize;
import static com.pepej.papi.text.Text.decolorize;
import static java.lang.String.format;


public class Listener implements TerminableModule {

    private static final Consumer<Cancellable> CANCELLED = e -> e.setCancelled(true);

    @NonNull
    public static <T extends PlayerEvent> Predicate<T> playerIsNotOp() {
        return e -> !e.getPlayer().isOp();
    }

    @Override
    public void setup(@NonNull final TerminableConsumer terminableConsumer) {
        UserService userService = Services.load(UserService.class);
        subscribe(PlayerJoinEvent.class)
                .handler(event -> {
                    event.setJoinMessage("");
                    event.getPlayer().setGameMode(GameMode.ADVENTURE);
                })
                .bindWith(terminableConsumer);

        subscribe(AsyncPlayerChatEvent.class)
              .handler(e -> {
                  LuckPerms luckPerms = Services.load(LuckPerms.class);
                  final net.luckperms.api.model.user.User user = luckPerms.getUserManager().getUser(e.getPlayer().getUniqueId());
                  final User u = userService.getUserNullable(e.getPlayer().getUniqueId());
                  if (user != null && u != null) {
                      String prefix = user.getCachedData().getMetaData().getPrefix();
                      String suffix = user.getCachedData().getMetaData().getSuffix();
                      prefix = prefix == null ? "" : prefix;
                      suffix = suffix == null ? "" : suffix;
                      e.setFormat(colorize(format("%s%s%s &7[%s]&8:&r %s", prefix, user.getUsername(), suffix, u.getLevelsReached(), decolorize(e.getMessage()))));
                  }
              }).bindWith(terminableConsumer);
        subscribe(BlockBreakEvent.class)
                .filter(e -> !e.getPlayer().isOp())
                .handler(e -> e.setCancelled(true))
                .bindWith(terminableConsumer);

        subscribe(BlockPlaceEvent.class)
                .filter(e -> !e.getPlayer().isOp())
                .handler(e -> e.setCancelled(true))
                .bindWith(terminableConsumer);
        subscribe(PlayerItemConsumeEvent.class)
                .filter(playerIsNotOp())
                .handler(CANCELLED)
                .bindWith(terminableConsumer);
        subscribe(PlayerDropItemEvent.class)
                .filter(playerIsNotOp())
                .handler(CANCELLED)
                .bindWith(terminableConsumer);
        subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getAction() == Action.LEFT_CLICK_BLOCK)
                .filter(playerIsNotOp())
                .handler(CANCELLED)
                .bindWith(terminableConsumer);
        subscribe(PlayerFishEvent.class)
                .filter(playerIsNotOp())
                .handler(CANCELLED)
                .bindWith(terminableConsumer);
        subscribe(PlayerBedEnterEvent.class)
                .filter(playerIsNotOp())
                .handler(CANCELLED)
                .bindWith(terminableConsumer);
        merge(PlayerBucketEvent.class, PlayerBucketFillEvent.class, PlayerBucketEmptyEvent.class)
                .filter(playerIsNotOp())
                .handler(CANCELLED)
                .bindWith(terminableConsumer);

        subscribe(EntityDamageByEntityEvent.class)
                .filter(e -> (e.getDamager() instanceof Player) && (e.getEntity() instanceof Player))
                .filter(e -> !e.getDamager().isOp())
                .handler(e -> {
                    if (!userService.getUserByPlayer((Player) e.getDamager()).flatMap(User::getCurrentArenaSafe).isPresent()
                    || !userService.getUserByPlayer((Player) e.getEntity()).flatMap(User::getCurrentArenaSafe).isPresent()) {
                        e.setCancelled(true);
                    }
                })
                .bindWith(terminableConsumer);
        subscribe(AsyncPlayerPreLoginEvent.class)
                .filter(e -> Players.getNullable(e.getUniqueId()) != null)
                .handler(e -> Schedulers.sync().run(() -> Players.get(e.getUniqueId()).ifPresent(player -> player.kickPlayer("logger from another location"))))
                .bindWith(terminableConsumer);
    }
}
