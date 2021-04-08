package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.Metadatas;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.bossbar.BossBar;
import com.pepej.papi.bossbar.BossBarColor;
import com.pepej.papi.bossbar.BossBarFactory;
import com.pepej.papi.event.filter.EventFilters;
import com.pepej.papi.metadata.Metadata;
import com.pepej.papi.metadata.MetadataMap;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.services.Services;
import com.pepej.papi.utils.Players;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Predicate;

import static com.pepej.gungame.Metadatas.*;
import static com.pepej.papi.events.Events.subscribe;
import static java.lang.String.format;

public class DeathJailTrap extends TrapBase {


    public DeathJailTrap() {
        super("death jail", TrapType.DEATH_JAIL);
    }

    @Override
    public void onActivate(@NonNull final User user, @NonNull final Arena arena) {
        Player player = user.asPlayer();
        MetadataMap metadataMap = Metadata.provideForPlayer(player);
        player.setWalkSpeed(0);
        player.setFlySpeed(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 300, 128, true, false), true);
        metadataMap.put(Metadatas.JAIL_TRAP, this);
        metadataMap.put(Metadatas.HELP_WRITE_COUNT, 3);
        getUserService().sendMessage(user, "&aВы должны написать 3 раза &c\"Памагити\"&a чтобы выбраться");
        getUserService().broadcastMessage(arena, format("&a%s&e попался в тюремную ловушку!", user.getUsername()));
        BossBarFactory factory = Services.load(BossBarFactory.class);
        subscribe(AsyncPlayerChatEvent.class)
                .filter(eventFilter(getUserService(), arena))
                .filter(EventFilters.playerHasMetadata(JAIL_TRAP))
                .filter(EventFilters.playerHasMetadata(HELP_WRITE_COUNT))
                .filter(e -> Metadata.provideForPlayer(e.getPlayer()).getOrDefault(HELP_WRITE_COUNT, 0) > 0)
                .filter(e -> e.getMessage().equalsIgnoreCase("памагити"))
                .handler(event -> {
                    int helpCount = metadataMap.getOrDefault(HELP_WRITE_COUNT, 0);
                    if (helpCount == 1) {
                        getUserService().sendMessage(user, "&aВы попросили о помощи. Если в течение 15 секунд к Вам прибежит игрок и кликнет по Вам, вы будете жить, иначе умрете");
                        getUserService().broadcastMessage(arena, format("&a%s&e умоляет о помощи, помогите ему нажав по нему ПКМ", user.getUsername()));
                        metadataMap.put(IS_WAITING_HELP, true);
                        metadataMap.put(JAIL_TRAP_DEATH_TIMER, 15);
                        metadataMap.remove(HELP_WRITE_COUNT);
                        final BossBar bossBar = factory.newBossBar();
                        bossBar.addPlayer(event.getPlayer());
                        bossBar.title("&aДо вашей смерти осталось:&c " + metadataMap.getOrDefault(JAIL_TRAP_DEATH_TIMER, 15) + "&a секунд")
                               .color(BossBarColor.RED)
                               .bindWith(arena);

                        Schedulers.sync()
                                  .runRepeating(task -> {
                                      if (!metadataMap.has(JAIL_TRAP)) {
                                          task.stop();
                                          bossBar.close();
                                          Players.resetWalkSpeed(event.getPlayer());
                                          Players.resetFlySpeed(event.getPlayer());
                                          event.getPlayer().removePotionEffect(PotionEffectType.JUMP);
                                      }
                                      else {
                                          bossBar.title("&aДо вашей смерти осталось:&c " + metadataMap.getOrDefault(JAIL_TRAP_DEATH_TIMER, 15) + "&a секунд");
                                          metadataMap.forcePut(JAIL_TRAP_DEATH_TIMER, metadataMap.getOrPut(JAIL_TRAP_DEATH_TIMER, () -> 15) - 1);
                                          if (metadataMap.getOrDefault(JAIL_TRAP_DEATH_TIMER, 15) <= 0) {
                                              event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 300, 10));
                                              event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 300, 10));
                                              Players.resetWalkSpeed(event.getPlayer());
                                              Players.resetFlySpeed(event.getPlayer());
                                              metadataMap.remove(JAIL_TRAP);
                                              bossBar.close();
                                              task.stop();

                                          }
                                      }
                                  }, 0, 20)
                                  .bindWith(arena);

                    } else {
                        getUserService().sendMessage(user, "&aВам осталось ещё " + (helpCount - 1) + " раз!");

                    }
                    metadataMap.put(HELP_WRITE_COUNT, Math.max(helpCount - 1, 0));

                })
                .bindWith(arena);
        subscribe(PlayerInteractAtEntityEvent.class)
                .filter(eventFilter(getUserService(), arena))
                .filter(e -> e.getRightClicked() instanceof Player)
                .filter(e -> Metadata.provideForPlayer((Player) e.getRightClicked()).has(IS_WAITING_HELP))
                .handler(e -> {
                    final Player rightClicked = (Player) e.getRightClicked();
                    final User rightClickedUser = getUserService().getUserByPlayerNullable(rightClicked);
                    final User clickerUser = getUserService().getUserByPlayerNullable(e.getPlayer());
                    if (rightClickedUser != null) {
                        getUserService().sendMessage(rightClickedUser, format("&7%s&a освободил вас!", clickerUser.getUsername()));
                        getUserService().sendMessage(clickerUser, format("&aВы освободили от заключения &7%s", rightClicked.getName()));
                    }
                    MetadataMap rightClickedMeta = Metadata.provideForPlayer(rightClicked);
                    rightClickedMeta.remove(JAIL_TRAP);
                    rightClickedMeta.remove(JAIL_TRAP_DEATH_TIMER);
                    rightClickedMeta.remove(IS_WAITING_HELP);
                })
                .bindWith(arena);

        subscribe(EntityDamageByEntityEvent.class, EventPriority.HIGH)
                .filter(e -> (e.getDamager() instanceof Player) && (e.getEntity() instanceof Player))
                .handler(event -> {
                    Player attacker = (Player) event.getDamager();
                    Player victim = (Player) event.getEntity();
                    User attackerUser = getUserService().getUserByPlayerNullable(attacker);
                    User victimUser = getUserService().getUserByPlayerNullable(victim);
                    if (attackerUser != null && attackerUser.getCurrentArena() != null && victimUser != null && victimUser.getCurrentArena() != null && arena.getState() == Arena.ArenaState.STARTED) {
                        MetadataMap attackerMeta = Metadata.provideForPlayer(attacker);
                        MetadataMap victimMeta = Metadata.provideForPlayer(victim);
                        if (victimMeta.has(JAIL_TRAP) || attackerMeta.has(JAIL_TRAP)) {
                            event.setCancelled(true);
                        }
                    }
                })
                .bindWith(arena);

    }

    private <T extends PlayerEvent> Predicate<T> eventFilter(UserService userService, Arena arena) {
        return e -> {

            User user = userService.getUserByPlayerNullable(e.getPlayer());
            if (user == null) {
                return false;
            }
            Arena currentArena = user.getCurrentArena();
            return currentArena != null && currentArena.equals(arena) && currentArena.getState() == Arena.ArenaState.STARTED;
        };
    }
}
