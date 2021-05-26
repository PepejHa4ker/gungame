package com.pepej.gungame.listener;

import com.pepej.gungame.GunGame;
import com.pepej.gungame.Metadatas;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.arena.loader.ArenaLoader;
import com.pepej.gungame.menu.ArenaSelectorMenu;
import com.pepej.gungame.menu.QuestSelectorMenu;
import com.pepej.gungame.rpg.trap.traps.SuperBowTrap;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.event.filter.EventFilters;
import com.pepej.papi.item.ItemStackBuilder;
import com.pepej.papi.metadata.ExpiringValue;
import com.pepej.papi.metadata.Metadata;
import com.pepej.papi.metadata.MetadataMap;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import com.pepej.papi.utils.Players;
import net.luckperms.api.LuckPerms;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.pepej.gungame.Metadatas.LAST_ATTACKER_KEY;
import static com.pepej.gungame.Metadatas.SUPER_SHOTS;
import static com.pepej.papi.events.Events.merge;
import static com.pepej.papi.events.Events.subscribe;
import static com.pepej.papi.text.Text.colorize;
import static com.pepej.papi.text.Text.decolorize;
import static java.lang.String.format;


public class Listener implements TerminableModule, org.bukkit.event.Listener {


    @EventHandler
    public void onEvent(PlayerInteractEvent event) {

        if (event.hasItem()) {
            ItemStack itemStack = event.getItem();
            if (itemStack.isSimilar(QUEST_SELECTOR)) {
                new QuestSelectorMenu(event.getPlayer()).open();
                event.setCancelled(true);
            } else if (itemStack.isSimilar(ARENA_SELECTOR)) {
                new ArenaSelectorMenu(event.getPlayer()).open();
                event.setCancelled(true);

            }
        }
    }

    public static final Consumer<Cancellable> CANCELLED = e -> e.setCancelled(true);

    public static final ItemStack ARENA_SELECTOR =
            ItemStackBuilder.head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzdlOGNiNTdmZTc5MGU5NjVlM2NmYTZjNGZiYzE2ZTMyMjYyMTBkNjVmNTYxNGU4ODUzZmE5ZmI4NDA3NDQ0MSJ9fX0=")
                                                                   .nameClickable("&aВыбор арены")
                                                                   .build();
    public static final ItemStack QUEST_SELECTOR = ItemStackBuilder.head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5Y2M1OGFkMjVhMWFiMTZkMzZiYjVkNmQ0OTNjOGY1ODk4YzJiZjMwMmI2NGUzMjU5MjFjNDFjMzU4NjcifX19=")
                                                                   .nameClickable("&dКвесты")
                                                                   .build();

    @NonNull
    public static <T extends PlayerEvent> Predicate<T> playerIsNotOp() {
        return e -> !e.getPlayer().isOp();
    }

    @Override
    public void setup(@NonNull final TerminableConsumer terminableConsumer) {
        UserService userService = Services.load(UserService.class);
        ArenaLoader arenaLoader = Services.load(ArenaLoader.class);
        subscribe(PlayerJoinEvent.class)
                .handler(event -> {
                    event.getPlayer().getInventory().setItem(0, ARENA_SELECTOR);
                    event.getPlayer().getInventory().setItem(4, QUEST_SELECTOR);
                    event.setJoinMessage("");
                    event.getPlayer().setGameMode(GameMode.ADVENTURE);
                    if (!event.getPlayer().isOp()) {
                        event.getPlayer().teleport(GunGame.getInstance().getGlobalConfig().getLobbyPosition().toLocation());
                    }
                })
                .bindWith(terminableConsumer);
        subscribe(PlayerQuitEvent.class)
                .handler(event -> {
                    event.setQuitMessage("");
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
                        e.setFormat(colorize(format("%s%s%s &7[%s]&8:&r %s", prefix, e.getPlayer().getName(), suffix, u.getLevelsReached(), decolorize(e.getMessage()))));
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
        subscribe(EntityDamageByEntityEvent.class)
                .filter(e -> e.getDamager() instanceof Arrow && e.getEntity() instanceof Player)
                .handler(e -> {
                    Player player = (Player) e.getEntity();
                    User user = userService.getUserByPlayerNullable(player);
                    if (user == null) {
                        return;
                    }
                    Arrow arrow = (Arrow) e.getDamager();
                    if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
                        Player shooter = (Player) arrow.getShooter();
                        if (shooter.equals(player)) {
                            arrow.remove();
                            e.setCancelled(true);
                            userService.sendMessage(user, "&cНе балуйся, так нельзя");
                        }
                    }
                })
                .bindWith(terminableConsumer);
        subscribe(ProjectileHitEvent.class)
                .filter(e -> e.getEntity() instanceof Arrow && e.getEntity().getShooter() != null && e.getEntity().getShooter() instanceof Player)
                .handler(event -> {
                    Arrow arrow = (Arrow) event.getEntity();
                    if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
                        Player attacker = (Player) arrow.getShooter();
                        MetadataMap attackerMap = Metadata.provideForPlayer(attacker);
                        int superShots = attackerMap.getOrDefault(SUPER_SHOTS, 0);
                        User user = userService.getUserByPlayerNullable(attacker);
                        if (user == null) {
                            return;
                        }
                        Arena currentArena = user.getCurrentArena();
                        if (currentArena == null || currentArena.getState() != Arena.ArenaState.STARTED) {
                            return;
                        }
                        if (event.getHitEntity() != null && event.getHitEntity() instanceof Player) {
                            Player victim = (Player) event.getHitEntity();
                            if (!victim.equals(attacker)) {
                                MetadataMap victimMeta = Metadata.provideForPlayer(victim);
                                victimMeta.forcePut(LAST_ATTACKER_KEY, ExpiringValue.of(attacker.getUniqueId(), 15, TimeUnit.SECONDS));
                                if (superShots >= 1) {
                                    attackerMap.put(SUPER_SHOTS, Math.max(superShots - 1, 0));
                                    victim.damage(100, attacker);
                                }
                            }
                        }
                        else {
                            attacker.getInventory().addItem(SuperBowTrap.SUPER_ARROW);
                        }
                    }

                    event.getEntity().remove();
                })
                .bindWith(terminableConsumer);
        subscribe(PlayerDropItemEvent.class)
                .filter(playerIsNotOp())
                .handler(CANCELLED)
                .bindWith(terminableConsumer);
//        subscribe(PlayerInteractEvent.class, EventPriority.HIGHEST)
//                .filter(e -> ARENA_SELECTOR.equals(e.getItem()))
//                .handler(e -> {
//
//                })
//                .bindWith(terminableConsumer);
//        subscribe(PlayerInteractEvent.class, EventPriority.HIGHEST)
//                .filter(e -> e.getItem() != null && QUEST_SELECTOR.equals(e.getItem()))
//                .handler(e -> {
//                    new QuestSelectorMenu(e.getPlayer()).open();
//                    e.setCancelled(true);
//                })
//                .bindWith(terminableConsumer);
        subscribe(FoodLevelChangeEvent.class)
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

        subscribe(AsyncPlayerChatEvent.class, EventPriority.HIGHEST)
                .filter(EventFilters.playerHasMetadata(Metadatas.CREATED_ARENA))
                .handler(e -> {
                    e.setCancelled(true);
                    final MetadataMap metadataMap = Metadata.provideForPlayer(e.getPlayer());

                    final ArenaConfig config = metadataMap.get(Metadatas.CREATED_ARENA).get()
                                                          .arenaName(e.getMessage())
                                                          .build();
                    arenaLoader.loadAndSaveArenaFromConfig(config);
                    metadataMap.remove(Metadatas.CREATED_ARENA);
                    userService.getUserByPlayer(e.getPlayer()).ifPresent(u -> userService.sendMessage(u, "&aАрена успешно создана!"));
                })
                .bindWith(terminableConsumer);
        subscribe(EntityDamageEvent.class)
                .filter(e -> e.getEntity() instanceof Player)
                .filter(e -> {
                    Player player = (Player) e.getEntity();
                    User user = userService.getUserByPlayerNullable(player);
                    if (user == null) {
                        return false;
                    }
                    return user.getCurrentArena() == null;
                })
                .handler(CANCELLED)
                .bindWith(terminableConsumer);

        subscribe(EntityDamageByEntityEvent.class)
                .filter(e -> (e.getDamager() instanceof Player) && (e.getEntity() instanceof Player))
                .handler(e -> {
                    final Player damager = (Player) e.getDamager();
                    final Player entity = (Player) e.getEntity();
                    final Arena damagerArena = userService.getUserByPlayer(damager).flatMap(User::getCurrentArenaSafe).orElse(null);
                    final Arena entityArena = userService.getUserByPlayer(entity).flatMap(User::getCurrentArenaSafe).orElse(null);
                    if (damagerArena == null || entityArena == null) {
                        e.setCancelled(true);
                    }
                    else {
                        if (!damagerArena.equals(entityArena)) {
                            e.setCancelled(true);
                        }
                        else {
                            if (entityArena.getState() != Arena.ArenaState.STARTED) {
                                e.setCancelled(true);
                            }
                        }
                    }
                })
                .bindWith(terminableConsumer);
        subscribe(AsyncPlayerPreLoginEvent.class)
                .filter(e -> Players.getNullable(e.getUniqueId()) != null)
                .handler(e -> Schedulers.sync().run(() -> Players.get(e.getUniqueId()).ifPresent(player -> player.kickPlayer("logger from another location"))))
                .bindWith(terminableConsumer);
    }
}
