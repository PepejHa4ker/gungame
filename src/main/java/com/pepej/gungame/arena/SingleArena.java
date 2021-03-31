package com.pepej.gungame.arena;

import com.pepej.gungame.GunGame;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.equipment.EquipmentResolver;
import com.pepej.gungame.model.Armor;
import com.pepej.gungame.repository.UserRepository;
import com.pepej.gungame.rpg.quest.QuestType;
import com.pepej.gungame.rpg.trap.TrapBase;
import com.pepej.gungame.service.QuestService;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.event.filter.EventFilters;
import com.pepej.papi.metadata.ExpiringValue;
import com.pepej.papi.metadata.Metadata;
import com.pepej.papi.metadata.MetadataMap;
import com.pepej.papi.promise.Promise;
import com.pepej.papi.random.RandomSelector;
import com.pepej.papi.random.VariableAmount;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.scoreboard.Scoreboard;
import com.pepej.papi.scoreboard.ScoreboardObjective;
import com.pepej.papi.serialize.Position;
import com.pepej.papi.serialize.Region;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.composite.CompositeTerminable;
import com.pepej.papi.utils.Log;
import com.pepej.papi.utils.Players;
import com.pepej.papi.utils.StringUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
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

import static com.pepej.gungame.Metadata.*;
import static com.pepej.papi.events.Events.subscribe;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class SingleArena implements Arena, TerminableConsumer {

    @NonFinal
    long timer;
    @NonFinal
    long startTimer;
    @NonNull World world;
    @NonNull ArenaContext context;
    @NonNull CompositeTerminable compositeTerminable;

    @NonNull UserService userService;
    @NonNull QuestService questService;

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
        resetTimers();

    }

    private void setStatus(@NonNull ArenaState state) {
        this.state = state;
    }

    @Override
    public void updateScoreboard(User user, ScoreboardObjective objective) {
        switch (state) {

            case WAITING:
                objective.applyLines(
                        format("&d Ожидание игроков &7(&6%s&a/&6%s&7)", context.getUsersCount(), context.getConfig().getMaxPlayers()),
                        "&1",
                        "&b  squareland.ru"
                );
                break;
            case DISABLED:
                objective.unsubscribeAll();
                break;
            case STARTING:

                objective.applyLines(
                        "&d Начало игры...",
                        "&1",
                        "&b  squareland.ru"
                );
                break;
            case STOPPING:
                objective.applyLines(
                        "&d Окончание игры...",
                        "&1",
                        "&b  squareland.ru"
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
                        "&b  squareland.ru"
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
                  .async()
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
        if (state == ArenaState.STARTED) {
            setStatus(ArenaState.STOPPING);
            Log.info("Stopping arena %s", context.getConfig().getArenaName());
            userService.getTopUser().ifPresent(u -> {
                u.setWins(u.getWins() + 1);
                userService.broadcastMessage(this, "&d---------------");
                userService.broadcastMessage(this, format("&cПобедитель: &b%s", u.getUsername()));
                userService.broadcastMessage(this, "&d---------------");
            });
            context.getUsers().forEach(u -> {
                u.setGamesPlayed(u.getGamesPlayed() + 1);
                questService.getActiveQuests(u, QuestType.PLAY_FIVE_GAMES).forEach(questService::onUpdate);
                leave(u, ArenaLeaveCause.END_OF_GAME);
            });
            resetTimers();
            setStatus(ArenaState.WAITING);
        }
    }

    @Override
    public void resetTimers() {
        this.timer = context.getConfig().getArenaGameTime().getSeconds() * 20;
        this.startTimer = context.getConfig().getArenaStartDelay().getSeconds() * 20;
    }

    @Override
    public void run() {
        context.getUsers()
               .stream()
               .filter(Objects::nonNull)
               .forEach(user -> {
                   Player player = user.asPlayer();
                   if (player != null) {
                       ScoreboardObjective objective = Metadata.provideForPlayer(player).getOrNull(SCOREBOARD_KEY);
                       updateScoreboard(user, objective);
                   }
               });

        switch (state) {
            case STARTING:
                if (context.getUsersCount() < context.getConfig().getRequiredPlayersToStart()) {
                    setStatus(ArenaState.WAITING);
                    resetTimers();
                }
                if (startTimer <= 0) {
                    start();
                    setStatus(ArenaState.STARTED);
                }
                if (startTimer % 20 == 0) {
                    for (User user : context.getUsers()) {
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
                context.getUsers().stream()
                       .filter(user -> user.getLocalLevelsReached() >= 12)
                       .findFirst()
                       .ifPresent($ -> stop());
                break;
        }

    }


    @Override
    public void join(@NonNull final User user) {
        Player player = user.asPlayer();
        if (context.getUsers().contains(user)) {
            return;
        }

        if (context.getUsersCount() >= context.getConfig().getMaxPlayers()) {
            return;
        }

        if (state == ArenaState.STOPPING || state == ArenaState.DISABLED) {
            return;
        }


        user.setLocalLevelsReached(1);
        player.setLevel(1);
        user.setCurrentArena(this);
        player.getInventory().clear();
        player.setExp(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);

        context.getUsers().add(user);
        ScoreboardObjective scoreboardObjective = context.getScoreboard().createPlayerObjective(player, "&bGunGame", DisplaySlot.SIDEBAR, false);
        Metadata.provideForPlayer(player)
                .forcePut(SCOREBOARD_KEY, scoreboardObjective);
        scoreboardObjective.subscribe(player);

        userService.broadcastMessage(this, format("&a + &d%s &7(&e%s&7/&c%s&7)", user.getUsername(), context.getUsersCount(), context.getConfig().getMaxPlayers()));
        if (context.getUsersCount() >= context.getConfig().getRequiredPlayersToStart() && state == ArenaState.WAITING) {
            setStatus(ArenaState.STARTING);


        }
        else if (state == ArenaState.STARTED) {
            context.getEquipmentResolver().equipUser(user, context.getEquipmentResolver().resolve(1));
        }
    }

    @Override
    public void leave(@NonNull User user, ArenaLeaveCause cause) {
        UserRepository userRepository = Services.load(UserRepository.class);
        Player player = user.asPlayer();
        Metadata.provideForPlayer(player).get(SCOREBOARD_KEY).ifPresent(obj -> obj.unsubscribe(player));
        user.setCurrentArena(null);
        user.setLocalLevelsReached(0);
        user.setLocalExp(0);
        user.setLocalKills(0);
        player.setExp(0);
        player.setLevel(0);
        player.getInventory().clear();
        Promise.completed(user)
               .thenAcceptAsync(u -> userService.sendMessage(u, "&7Подождите... Сохраняю Ваши данные"))
               .thenRunSync(() -> userRepository.updateUser(
                       user.getId().toString(),
                       user.getGamesPlayed(),
                       user.getWins(),
                       user.getKills(),
                       user.getDeaths(),
                       user.getLevelsReached(),
                       user.getExp()))
               .thenRunSync(() -> {
                   userService.sendMessage(user, "&aВаши данные успешно сохранены!");
                   userService.broadcastMessage(this, format("&a - &d%s &7(&e%s&7/&c%s&7)", user.getUsername(), context.getUsersCount(), context.getConfig().getMaxPlayers()));
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
        subscribe(PlayerMoveEvent.class)

                .filter(e -> userService.getUserByPlayer(e.getPlayer()).flatMap(User::getCurrentArenaSafe).isPresent() && state == ArenaState.STARTED)
                .filter(EventFilters.ignoreSameBlockAndY())
                .handler(event -> {
                    MetadataMap metadataMap = Metadata.provideForPlayer(event.getPlayer());
                    metadataMap.forcePut(CAN_BE_ATTACKED_KEY, true);
                })
                .bindWith(this);

        subscribe(EntityDamageByEntityEvent.class)
                .filter(e -> (e.getDamager() instanceof Player) && (e.getEntity() instanceof Player))

                .handler(event -> {
                    Player attacker = (Player) event.getDamager();
                    Optional<User> attackerUser = userService.getUserByPlayer(attacker);
                    if (userService.getUserByPlayer(attacker).flatMap(User::getCurrentArenaSafe).isPresent() && state == ArenaState.STARTED) {
                        Player victim = (Player) event.getEntity();
                        MetadataMap attackerMeta = Metadata.provideForPlayer(attacker);
                        attackerMeta.forcePut(CAN_BE_ATTACKED_KEY, true);
                        MetadataMap victimMeta = Metadata.provideForPlayer(victim);
//                        if (victimMeta.getOrDefault(CAN_BE_ATTACKED_KEY, true)) {
                            victimMeta.forcePut(LAST_ATTACKER_KEY, ExpiringValue.of(attacker.getUniqueId(), 15, TimeUnit.SECONDS));
//                        }
//                        else {
//                            event.setCancelled(true);
//                            attackerUser.ifPresent(us -> {
//                                userService.sendMessage(us, "&cИгрок в No-PvP режиме!");
//                            });
//                        }
                    }
                })
                .bindWith(this);
        subscribe(ProjectileHitEvent.class)
                .filter(e -> e.getEntity() instanceof Arrow && e.getHitEntity() != null && e.getHitEntity() instanceof Player)
                .handler(event -> {
                    Arrow arrow = (Arrow) event.getEntity();
                    if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
                        Player attacker = (Player) arrow.getShooter();
                        if (userService.getUserByPlayer(attacker).flatMap(User::getCurrentArenaSafe).isPresent() && state == ArenaState.STARTED) {
                            Player victim = (Player) event.getHitEntity();
                            MetadataMap victimMeta = Metadata.provideForPlayer(victim);
                            victimMeta.forcePut(LAST_ATTACKER_KEY, ExpiringValue.of(attacker.getUniqueId(), 15, TimeUnit.SECONDS));
                        }
                    }
                })
                .bindWith(this);

        subscribe(PlayerDeathEvent.class)
                .filter(e -> userService.getUserByPlayer(e.getEntity()).flatMap(User::getCurrentArenaSafe).isPresent() && state == ArenaState.STARTED)
                .handler(event -> {
                    event.setDeathMessage("" );
                    User dead = userService.getUserByPlayerNullable(event.getEntity());
                    MetadataMap metadataMap = Metadata.provideForPlayer(event.getEntity());
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
                                   context.getUsers().forEach(us -> {
                                       userService.sendMessage(us, format("&a%s &6убит игроком &c%s", event.getEntity().getName(), u.getUsername()));
                                   });
                               });
                               if (!user.isPresent()) {
                                   context.getUsers().forEach(us -> userService.sendMessage(us, format("&a%s&e самоубился :(", dead.getUsername())));
                               }
                           })
                           .thenRunDelayedSync(() -> event.getEntity().spigot().respawn(), 3);
                })
                .bindWith(this);

        subscribe(PlayerRespawnEvent.class)
                .filter(e -> userService.getUserByPlayer(e.getPlayer()).flatMap(User::getCurrentArenaSafe).isPresent() && state == ArenaState.STARTED)
                .handler(event -> {
                    MetadataMap metadataMap = Metadata.provideForPlayer(event.getPlayer());
                    metadataMap.forcePut(CAN_BE_ATTACKED_KEY, false);
                    Position spawn = RandomSelector.uniform(userService.getUserByPlayerNullable(event.getPlayer()).getCurrentArena().getContext().getConfig().getPositions()).pick();
                    event.setRespawnLocation(spawn.toLocation());
                    event.getPlayer().setNoDamageTicks(60);
                })
                .bindWith(this);

        subscribe(PlayerLevelChangeEvent.class)
                .filter(e -> userService.getUserByPlayer(e.getPlayer()).flatMap(User::getCurrentArenaSafe).isPresent() && state == ArenaState.STARTED)
                .handler(event -> {
                    if (event.getNewLevel() == 1) {
                        return;
                    }
                    Optional<User> user = userService.getUserByPlayer(event.getPlayer());
                    user.ifPresent(u -> {
                        Player player = u.asPlayer();
                        int levelsReached = (event.getNewLevel() - event.getOldLevel());
                        context.getUsers().forEach(us -> userService.sendMessage(us, format("&d%s&a levelup &c%s &7-> &c%s",
                                u.getUsername(),
                                u.getLocalLevelsReached(),
                                u.getLocalLevelsReached() + levelsReached)));
                        u.setLevelsReached(u.getLevelsReached() + levelsReached);
                        u.setLocalLevelsReached(u.getLocalLevelsReached() + levelsReached);
                        final Armor armor = context.getEquipmentResolver().resolve(u.getLocalLevelsReached());
                        context.getEquipmentResolver().equipUser(u, armor);
                        userService.sendTitle(u, "&aLevelup!!!");
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 2), true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 1), true);
                        Players.spawnParticle(u.asPlayer().getEyeLocation(), Particle.VILLAGER_HAPPY);
                    });


                })
                .bindWith(this);
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

        @NonNull EquipmentResolver equipmentResolver;
        @NonNull Duration arenaStartDuration;
        @NonNull ArenaConfig config;
        @NonNull Scoreboard scoreboard;
        @NonNull UserService userService;
        @NonNull Set<User> users;
        @NonNull Map<Region, TrapBase> traps;

        public SingleArenaContext(final ArenaConfig config, final @NonNull Scoreboard scoreboard, @NonNull final EquipmentResolver equipmentResolver, final @NonNull Map<Region, TrapBase> traps) {
            this.config = config;
            this.scoreboard = scoreboard;
            this.arenaStartDuration = config.getArenaStartDelay();
            this.equipmentResolver = equipmentResolver;
            this.traps = traps;
            this.users = Collections.newSetFromMap(new ConcurrentHashMap<>(config.getMaxPlayers()));
            ; //pre-initialize set capacity to max possible users per arena
            this.userService = Services.load(UserService.class);

        }

        @Override
        public int getMaxUserLevel() {
            return userService.getTopUser().map(User::getLocalLevelsReached).orElse(1);
        }
    }
}
