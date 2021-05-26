package com.pepej.gungame.arena;

import com.pepej.gungame.GunGame;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.equipment.EquipmentResolver;
import com.pepej.gungame.model.Armor;
import com.pepej.gungame.repository.UserRepository;
import com.pepej.gungame.rpg.quest.QuestType;
import com.pepej.gungame.rpg.trap.GunGameTrap;
import com.pepej.gungame.rpg.trap.Trap;
import com.pepej.gungame.rpg.trap.handler.TrapHandler;
import com.pepej.gungame.service.QuestService;
import com.pepej.gungame.service.TrapService;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.metadata.ExpiringValue;
import com.pepej.papi.metadata.Metadata;
import com.pepej.papi.metadata.MetadataMap;
import com.pepej.papi.promise.Promise;
import com.pepej.papi.random.RandomSelector;
import com.pepej.papi.random.VariableAmount;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.scoreboard.Scoreboard;
import com.pepej.papi.scoreboard.ScoreboardObjective;
import com.pepej.papi.serialize.Point;
import com.pepej.papi.serialize.Position;
import com.pepej.papi.serialize.Region;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.composite.CompositeTerminable;
import com.pepej.papi.utils.Log;
import com.pepej.papi.utils.Players;
import com.pepej.papi.utils.StringUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.pepej.gungame.Metadatas.*;
import static com.pepej.gungame.listener.Listener.ARENA_SELECTOR;
import static com.pepej.gungame.listener.Listener.QUEST_SELECTOR;
import static com.pepej.gungame.utils.DonatUtils.applyIfVip;
import static com.pepej.papi.events.Events.subscribe;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class SingleArena implements Arena {

    @NonFinal
    long timer;
    @NonFinal
    long startTimer;
    @NonFinal
    long stopTimer;

    @Override
    public Point getRandomPositionToSpawn() {
        return RandomSelector.uniform(context.getConfig().getPositions()).pick();
    }

    @NonNull World world;
    @NonNull ArenaContext context;
    @NonNull CompositeTerminable compositeTerminable;

    @NonNull UserService userService;
    @NonNull QuestService questService;
    @NonNull TrapHandler trapHandler;

    @NonNull
    @NonFinal
    ArenaState state;


    public SingleArena(final @NonNull World world, final @NonNull ArenaContext context) {
        this.world = world;
        this.context = context;
        this.state = ArenaState.DISABLED;
        this.compositeTerminable = CompositeTerminable.create();
        this.userService = Services.load(UserService.class);
        this.questService = Services.load(QuestService.class);
        this.trapHandler = user -> {
            if (user.asPlayer() != null) {
                final Position position = Position.of(user.location());
                getContext().getTraps().entrySet().stream()
                            .filter(e -> e.getKey().inRegion(position))
                            .findFirst()
                            .map(Map.Entry::getValue)
                            .filter(trap -> trap.getCooldown().test())
                            .ifPresent(t -> t.onActivate(user, this));
            }
        };
        resetTimers();

    }

    private void setStatus(@NonNull ArenaState state) {
        this.state = state;
    }

    @Override
    public void updateMemberScoreboard(User user, ScoreboardObjective objective) {
        switch (state) {

            case WAITING:
                objective.applyLines(
                        format("&d Ожидание игроков &7(&6%s&a/&6%s&7)", context.getUsersCount(), context.getConfig().getMaxPlayers()),
                        "&1",
                        "&b  Squareland.ru"
                );
                break;
            case DISABLED:
                objective.unsubscribeAll();
                break;
            case STARTING:

                objective.applyLines(
                        "&d Начало игры...",
                        "&1",
                        "&b  Squareland.ru"
                );
                break;
            case PRE_STOPPING:
            case STOPPING:
                objective.applyLines(
                        "&d Окончание игры...",
                        "&1",
                        "&b  Squareland.ru"
                );
                break;
            case STARTED:
                objective.applyLines(
                        format("         &d%s", getTimerColor(timer / 20) + formatSeconds(timer / 20)),
                        "&1 ",
                        format(" &a Убийств: &c%s &7(&d%s&7)", user.getLocalKills(), user.getKills()),
                        format(" &a Текущий уровень: &c%s &7(&d%s&7)", user.getLocalLevelsReached(), user.getLevelsReached()),
                        "&2",

                        format("    &d#1 &7->&a %s &7(&a%s&7)",
                                userService.getTopUser().map(User::getUsername).orElse("----"),
                                userService.getTopUser().map(User::getLocalLevelsReached).orElse(0)),
                        "&b  Squareland.ru"
                );
                break;
        }
    }

    @Override
    public void enable() {
        if (state != ArenaState.DISABLED) {
            Log.warn("To enable the arena, it must have state = DISABLED. Current state = " + state);
            return;
        }

        Log.info("Enabling arena %s", context.getConfig().getArenaName());
        setStatus(ArenaState.WAITING);
        Schedulers.builder()
                  .sync()
                  .every(1)
                  .run(this)
                  .bindWith(this);
        startListening();
    }

    @Override
    public void disable() {
        compositeTerminable.closeAndReportException();
        setStatus(ArenaState.DISABLED);
    }

    @Override
    public void start() {
        for (User user : context.getUsers()) {
            userService.sendMessage(user, "&bИгра началась!");
            user.teleport(context.getConfig().getStartPosition());
            final Armor armor = context.getEquipmentResolver().resolve(1);
            context.getEquipmentResolver().equipUser(user, armor);
        }
    }

    @Override
    public void stop() {
        ArenaState stateBefore = getState();
        Promise.start()
               .thenApplySync($ -> userService.getTopUser())
               .thenAcceptAsync(user -> user.ifPresent(u -> {
                   if (stateBefore == ArenaState.STARTED) {
                       u.setWins(u.getWins() + 1);
                       userService.broadcastMessage(this, format("&cПобедитель: &b%s", u.getUsername()));
                       for (final User usr : userService.getAllUsers()) {
                           final Player player = usr.asPlayer();
                           if (player != null) {
                               Players.playSound(player, Sound.ENTITY_ELDER_GUARDIAN_CURSE);
                           }
                           if (!context.getUsers().contains(usr)) {
                               userService.sendMessage(usr, format("&aИгрок &6%s&a победил на арене &c%s", u.getUsername(), context.getConfig().getArenaName()));
                           }
                       }
                   }
               }))
               .thenApplyAsync($ -> context.getUsers())
               .thenAcceptAsync(users -> users.forEach(u -> {
                   if (stateBefore == ArenaState.STARTED) {
                       u.setGamesPlayed(u.getGamesPlayed() + 1);
                       questService.getActiveQuests(u, QuestType.PLAY_FIVE_GAMES).forEach(questService::onUpdate);
                   }
               }))
               .thenRunAsync(() -> {
                   setStatus(ArenaState.PRE_STOPPING);
                   userService.broadcastMessage(this, format("&eИгра закончена. Вы будете телепортированы на спавн через %s секунд.", this.stopTimer / 20));
               })
               .thenApplyAsync($ -> context.getUsers())
               .thenAcceptSync(users -> users.forEach(u -> leave(u, ArenaLeaveCause.END_OF_GAME)))
               .thenRunDelayedSync(() -> setStatus(ArenaState.STOPPING), stopTimer)
               .thenRunSync(() -> {
                   if (stateBefore == ArenaState.STARTED) {
                       Log.info("Stopping arena %s", context.getConfig().getArenaName());
                   }
               })

               .thenRunAsync(this::resetTimers)
               .thenRunAsync(() -> setStatus(ArenaState.WAITING));

    }


    @Override
    public void resetTimers() {
        this.timer = context.getConfig().getArenaGameTime().getSeconds() * 20;
        this.startTimer = context.getConfig().getArenaStartDelay().getSeconds() * 20;
        this.stopTimer = context.getConfig().getArenaStopDelay().getSeconds() * 20;
    }

    @Override
    public void run() {
        context.getUsers()
               .stream()
               .filter(Objects::nonNull)
               .filter(user -> !user.isSpectator())
               .forEach(user -> {
                   Player player = user.asPlayer();
                   if (player != null) {
                       ScoreboardObjective objective = Metadata.provideForPlayer(player).getOrNull(SCOREBOARD_KEY);
                       updateMemberScoreboard(user, objective);
                   }
               });

        switch (state) {
            case STARTING:
                if (context.getUsersCount() < context.getConfig().getRequiredPlayersToStart()) {
                    setStatus(ArenaState.WAITING);
                    resetTimers();
                }
                if (startTimer <= 0) {
                    for (User user : context.getUsers()) {
                        final Player player = user.asPlayer();
                        if (player != null) {
                            Players.playSound(player, Sound.ENTITY_ILLUSION_ILLAGER_DEATH);
                        }
                    }
                    start();
                    setStatus(ArenaState.STARTED);
                }
                if (startTimer % 20 == 0 && startTimer >= 20) {
                    for (User user : context.getUsers()) {
                        final Player player = user.asPlayer();
                        if (player != null) {
                            Players.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                        }
                        userService.sendTitle(user, format("&7Игра начнется через %s%s &7%s",
                                getStartTimerColor(startTimer / 20),
                                startTimer / 20,
                                pluralSeconds((int) (startTimer / 20))
                        ));

                    }
                }
                startTimer -= 1;
                break;
            case STARTED:
                if (timer == 400 || timer == 300 || timer == 200 || (timer <= 100) && timer % 20 == 0) {
                    userService.broadcastMessage(this, format("&eИгра закончится через %s %s", timer / 20, pluralSeconds((int) (timer / 20))));


                }
                timer -= 1;
                if (timer <= 0) {
                    stop();
                }
                for (User user : context.getUsers()) {
                    trapHandler.handle(user);
                }
                context.getUsers().stream()
                       .filter(user -> user.getLocalLevelsReached() >= 12)
                       .findFirst()
                       .ifPresent($ -> stop());
                break;
        }

    }


    @Override
    public void join(@NonNull final User user, ArenaJoinType joinType) {
        Player player = user.asPlayer();
        if (context.getUsers().contains(user) || user.getCurrentArena() != null) {
            return;
        }
        context.getUsers().add(user);


        if (context.getUsersCount() >= context.getConfig().getMaxPlayers()) {
            return;
        }

        if (state == ArenaState.STOPPING || state == ArenaState.DISABLED) {
            return;
        }

        if (joinType == ArenaJoinType.MEMBER) {
            user.setSpectator(false);
            user.setLocalLevelsReached(1);
            player.setLevel(1);
            player.getInventory().clear();
            player.setExp(0);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setGameMode(GameMode.ADVENTURE);
            ScoreboardObjective scoreboardObjective = context.getScoreboard().createPlayerObjective(player, "&bGunGame", DisplaySlot.SIDEBAR, false);
            Metadata.provideForPlayer(player)
                    .forcePut(SCOREBOARD_KEY, scoreboardObjective);
            scoreboardObjective.subscribe(player);

            userService.broadcastMessage(this, format("&a+ &d%s &7(&e%s&7/&c%s&7)", user.getUsername(), context.getUsersCount(), context.getConfig().getMaxPlayers()));
            if (state == ArenaState.WAITING && context.getUsersCount() >= context.getConfig().getRequiredPlayersToStart()) {
                setStatus(ArenaState.STARTING);


            }
            else if (state == ArenaState.STARTED) {
                context.getEquipmentResolver().equipUser(user, context.getEquipmentResolver().resolve(1));

                Point spawn = getRandomPositionToSpawn();
                user.teleport(spawn);
            }
        }
        else {
            user.teleport(context.getConfig().getStartPosition());
            user.setSpectator(true);
            Schedulers.sync().run(() -> context.getUsers().stream()
                                               .filter(u -> !u.isSpectator())
                                               .map(User::asPlayer)
                                               .filter(Objects::nonNull)
                                               .forEach(p -> p.hidePlayer(GunGame.getInstance(), player)));
            player.setAllowFlight(true);
            player.setCollidable(false);
            userService.broadcastMessage(this, format("&a+ &d%s &7(Наблюдатьель)", user.getUsername()));


        }
        player.setCanPickupItems(false);
        user.setCurrentArena(this);

    }

    @Override
    public void leave(@NonNull User user, ArenaLeaveCause cause) {
        UserRepository userRepository = Services.load(UserRepository.class);
        Player player = user.asPlayer();
        if (player != null) {
            Metadata.provideForPlayer(player).get(SCOREBOARD_KEY).ifPresent(obj -> obj.unsubscribe(player));
            player.setExp(0);
            player.setLevel(0);

            player.setHealth(20);
            player.getInventory().clear();
            player.getInventory().setItem(0, ARENA_SELECTOR);
            player.getInventory().setItem(4, QUEST_SELECTOR);
            Schedulers.sync().run(() -> {
                player.setAllowFlight(false);
                context.getUsers().stream().filter(u -> !u.isSpectator()).map(User::asPlayer).filter(Objects::nonNull).forEach(p -> p.showPlayer(GunGame.getInstance(), player));
            });
        }
        user.setSpectator(false);
        user.setCurrentArena(null);
        user.setLocalLevelsReached(0);
        user.setLocalExp(0);
        user.setLocalKills(0);

        Promise.completed(user)
               .thenAcceptAsync(u -> userService.sendMessage(u, "&7Подождите... Сохраняю Ваши данные"))
               .thenRunAsync(() -> userRepository.updateUser(
                       user.getId().toString(),
                       user.getGamesPlayed(),
                       user.getWins(),
                       user.getKills(),
                       user.getDeaths(),
                       user.getLevelsReached(),
                       user.getExp()))
               .thenRunSync(() -> {
                   userService.sendMessage(user, "&aВаши данные успешно сохранены!");
                   if (cause == ArenaLeaveCause.FORCE) {
                       userService.broadcastMessage(this, format("&a - &d%s &7(&e%s&7/&c%s&7)", user.getUsername(), context.getUsersCount(), context.getConfig().getMaxPlayers()));
                   }
                   context.getUsers().remove(user);
                   user.teleport(GunGame.getInstance().getGlobalConfig().getLobbyPosition());
                   if (context.getUsersCount() <= 1) {
                       stop();
                   }
               });
    }

    @Override
    public <T extends AutoCloseable> @NonNull T bind(@NonNull final T terminable) {
        return compositeTerminable.bind(terminable);
    }

    @Override
    public void startListening() {
        subscribe(PlayerPickupArrowEvent.class)
                .filter(eventFilter(userService))
                .handler(e -> {
                    e.setCancelled(true);
                    e.getArrow().remove();
                })
                .bindWith(this);
        subscribe(PlayerTeleportEvent.class)
                .filter(eventFilter(userService))
                .handler(e -> e.getPlayer().setNoDamageTicks(0))
                .bindWith(this);
        subscribe(PlayerMoveEvent.class)
                .filter(eventFilter(userService))
                .handler(event -> {
                    final Player player = event.getPlayer();
                    MetadataMap metadataMap = Metadata.provideForPlayer(player);
                    metadataMap.forcePut(CAN_BE_ATTACKED_KEY, ExpiringValue.of(true, 10, TimeUnit.SECONDS));


                })
                .bindWith(this);

        subscribe(EntityDamageByEntityEvent.class)
                .filter(e -> (e.getDamager() instanceof Player) && (e.getEntity() instanceof Player))
                .handler(event -> {
                    Player attacker = (Player) event.getDamager();
                    Player victim = (Player) event.getEntity();
                    User attackerUser = userService.getUserByPlayerNullable(attacker);
                    User victimUser = userService.getUserByPlayerNullable(victim);
                    if (attackerUser != null && attackerUser.getCurrentArena() != null && victimUser != null && victimUser.getCurrentArena() != null && state == ArenaState.STARTED) {
                        if (attackerUser.isSpectator() || victimUser.isSpectator()) {
                            event.setCancelled(true);
                            return;
                        }
                        MetadataMap attackerMeta = Metadata.provideForPlayer(attacker);
                        MetadataMap victimMeta = Metadata.provideForPlayer(victim);
                        attackerMeta.forcePut(CAN_BE_ATTACKED_KEY, ExpiringValue.of(true, 10, TimeUnit.SECONDS));
                        if (victimMeta.getOrDefault(CAN_BE_ATTACKED_KEY, true)) {
                            victimMeta.forcePut(LAST_ATTACKER_KEY, ExpiringValue.of(attacker.getUniqueId(), 15, TimeUnit.SECONDS));
                        }
                        else {
                            event.setCancelled(true);
                            userService.sendMessage(attackerUser, "&cИгрок в No-PvP режиме!");
                        }
                    } else {
                        event.setCancelled(true);
                    }


                })
                .bindWith(this);
        subscribe(PlayerDeathEvent.class)
                .filter(e -> {
                    User user = userService.getUserByPlayerNullable(e.getEntity());
                    if (user == null) {
                        return false;
                    }
                    Arena currentArena = user.getCurrentArena();
                    return currentArena != null && currentArena.equals(this) && currentArena.getState() == ArenaState.STARTED;
                })
                .handler(event -> {
                    event.setDeathMessage("");
                    User dead = userService.getUserByPlayerNullable(event.getEntity());
                    MetadataMap metadataMap = Metadata.provideForPlayer(event.getEntity());
                    metadataMap.remove(JAIL_TRAP);
                    Promise.start()
                           .thenApplySync($ -> userService.getUserByPlayerNullable(event.getEntity()))
                           .thenAcceptSync(user -> user.setDeaths(user.getDeaths() + 1))
                           .thenApplySync($ -> metadataMap.getOrNull(LAST_ATTACKER_KEY))
                           .thenApplySync(Players::get)
                           .thenApplySync(userService::getUserByPlayer)
                           .thenAcceptAsync(user -> {
                               user.ifPresent(u -> {
                                   u.setKills(u.getKills() + 1);
                                   questService.getActiveQuests(u, QuestType.KILL_TEN_PLAYER).forEach(questService::onUpdate);
                                   questService.getActiveQuests(u, QuestType.KILL_HUNDRED_PLAYER).forEach(questService::onUpdate);
                                   u.setLocalKills(u.getLocalKills() + 1);
                                   userService.sendBossBar(u, format("&6Вы убили %s", event.getEntity().getName()));
                                   int expToGive = VariableAmount.range(4, 7).getFlooredAmount();
                                   userService.sendTitle(u, format("&a +%s опыта", expToGive));
                                   u.asPlayer().giveExp(expToGive);
                                   u.setLocalExp(u.getLocalExp() + expToGive);
                                   u.setExp(u.getExp() + expToGive);
                                   Schedulers.sync().run(() -> {
                                       Player player = u.asPlayer();
                                       if (player != null) {
                                           player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 1), true);
                                           player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1), true);
                                           applyIfVip(player, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0), true));
                                       }

                                   });


                                   context.getUsers().forEach(us -> {
                                       userService.sendMessage(us, format("&c%s &6убит игроком &a%s", event.getEntity().getName(), u.getUsername()));
                                   });
                               });
                               if (!user.isPresent()) {
                                   context.getUsers().forEach(us -> userService.sendMessage(us, format("&a%s&e самоубился :(", dead.getUsername())));
                               }
                           })
                           .thenRunDelayedSync(() -> event.getEntity().spigot().respawn(), 10);
                })
                .bindWith(this);

        subscribe(PlayerRespawnEvent.class)
                .filter(eventFilter(userService))
                .handler(event -> {
                    MetadataMap metadataMap = Metadata.provideForPlayer(event.getPlayer());
                    metadataMap.forcePut(CAN_BE_ATTACKED_KEY, ExpiringValue.of(false, 10, TimeUnit.SECONDS));
                    Point spawn = getRandomPositionToSpawn();
                    event.setRespawnLocation(spawn.toLocation());
                })
                .bindWith(this);


        subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
                .filter(e -> {
                    User user = userService.getUserByPlayerNullable(e.getPlayer());
                    if (user == null) {
                        return false;
                    }
                    Arena currentArena = user.getCurrentArena();
                    return currentArena != null && currentArena.equals(this) && currentArena.getState() == ArenaState.STARTED;
                })
                .handler(e -> e.setCancelled(true))
                .bindWith(this);

        subscribe(PlayerLevelChangeEvent.class)
                .filter(eventFilter(userService))
                .handler(event -> {
                    if (event.getNewLevel() == 1) {
                        return;
                    }
                    Optional<User> user = userService.getUserByPlayer(event.getPlayer());
                    user.ifPresent(u -> {
                        Player player = u.asPlayer();
                        if (player != null) {
                            int levelsReached = (event.getNewLevel() - event.getOldLevel());
                            context.getUsers().forEach(us -> userService.sendMessage(us, format("&d%s&a &c%s &7-> &c%s",
                                    u.getUsername(),
                                    u.getLocalLevelsReached(),
                                    u.getLocalLevelsReached() + levelsReached)));
                            u.setLevelsReached(u.getLevelsReached() + levelsReached);
                            u.setLocalLevelsReached(u.getLocalLevelsReached() + levelsReached);
                            final Armor armor = context.getEquipmentResolver().resolve(u.getLocalLevelsReached());
                            Players.playSound(player, Sound.ENTITY_ILLUSION_ILLAGER_CAST_SPELL);
                            context.getEquipmentResolver().equipUser(u, armor);
                            userService.sendTitle(u, "&aLevelup!");
                            Players.spawnParticle(u.asPlayer().getEyeLocation(), Particle.VILLAGER_HAPPY);
                        }
                    });


                })
                .bindWith(this);
    }

    private <T extends PlayerEvent> Predicate<T> eventFilter(UserService userService) {
        return e -> {

            User user = userService.getUserByPlayerNullable(e.getPlayer());
            if (user == null) {
                return false;
            }
            Arena currentArena = user.getCurrentArena();
            return currentArena != null && currentArena.equals(this) && currentArena.getState() == ArenaState.STARTED;
        };
    }

    private static String formatSeconds(long secs) {
        long millis = secs * 1000L;
        DateFormat df = new SimpleDateFormat("mm:ss");
        return df.format(millis);
    }

    private static String getTimerColor(long timer) {
        if (timer >= 31 && timer <= 60) {
            return "&6";
        }
        else if (timer >= 0 && timer <= 30) {
            return "&c";
        }
        return "&a";
    }

    private static String getStartTimerColor(long timer) {
        if (timer >= 11 && timer <= 15) {
            return "&a";
        }
        else if (timer >= 6 && timer <= 10) {
            return "&d";
        }
        return "&c";
    }

    private static String pluralSeconds(int count) {
        return StringUtils.plural(count, "секунду", "секунды", "секунд");

    }


    @ToString
    @EqualsAndHashCode
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    public static class SingleArenaContext implements ArenaContext {

        @NonNull
        TrapService trapService;
        @NonNull EquipmentResolver equipmentResolver;
        @NonNull Duration arenaStartDuration;
        @Setter
        @NonFinal
        @NonNull ArenaConfig config;
        @NonNull Scoreboard scoreboard;
        @NonNull UserService userService;
        @NonNull Set<User> users;
        @NonNull Map<Region, Trap> traps;

        public SingleArenaContext(final ArenaConfig config, final @NonNull Scoreboard scoreboard, @NonNull final EquipmentResolver equipmentResolver) {
            this.trapService = Services.load(TrapService.class);
            this.config = config;
            this.scoreboard = scoreboard;
            this.arenaStartDuration = config.getArenaStartDelay();
            this.equipmentResolver = equipmentResolver;
            this.users = Collections.newSetFromMap(new ConcurrentHashMap<>(config.getMaxPlayers()));
            ; //pre-initialize set capacity to max possible users per arena
            this.userService = Services.load(UserService.class);
            this.traps = config.getTraps().stream().collect(toMap(GunGameTrap::getRegion, t -> trapService.createTrapByType(t.getType())));
            this.traps.values().forEach(trap -> trap.bindWith((TerminableConsumer) this));

        }


        @Override
        public int getMaxUserLevel() {
            return userService.getTopUser().map(User::getLocalLevelsReached).orElse(1);
        }
    }
}
