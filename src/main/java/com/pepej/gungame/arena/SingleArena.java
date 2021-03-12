package com.pepej.gungame.arena;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.equipment.EquipmentResolver;
import com.pepej.gungame.model.Armor;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.metadata.ExpiringValue;
import com.pepej.papi.metadata.Metadata;
import com.pepej.papi.metadata.MetadataMap;
import com.pepej.papi.promise.Promise;
import com.pepej.papi.random.RandomSelector;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.scoreboard.ScoreboardObjective;
import com.pepej.papi.serialize.Position;
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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.pepej.gungame.Metadata.LAST_ATTACKER_KEY;
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

    @NonNull
    @NonFinal
    ArenaState state;


    public SingleArena(final @NonNull World world, final @NonNull ArenaContext context) {
        this.world = world;
        this.context = context;
        this.state = ArenaState.DISABLED;
        this.compositeTerminable = CompositeTerminable.create();
        this.userService = Services.load(UserService.class);
        resetTimers();

    }

    private void setStatus(@NonNull ArenaState state) {
        this.state = state;
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
            userService.sendMessage(user, "Game started");
            user.teleport(context.getConfig().getStartPosition());
            final Armor armor = context.getEquipmentResolver().resolve(1);
            context.getEquipmentResolver().equipUser(user, armor);
        }
    }

    @Override
    public void stop() {
        setStatus(ArenaState.STOPPING);
        Log.info("Stopping arena %s", context.getConfig().getArenaName());
        context.getUsers().forEach(u -> leave(u, ArenaLeaveCause.END_OF_GAME));
        context.getUsers().clear();
        resetTimers();
        setStatus(ArenaState.WAITING);
    }

    @Override
    public void resetTimers() {
        this.timer = context.getConfig().getArenaGameTime().getSeconds() * 20;
        this.startTimer = context.getConfig().getArenaStartDelay().getSeconds() * 20;
    }

    @Override
    public void run() {
        final ScoreboardObjective scoreboardObjective = context.getScoreboardObjective();

        switch (state) {

            case WAITING:
                scoreboardObjective.applyLines(
                        format("&d Ожидание игроков &7(&6%s&a/&6%s&7)", context.getUsersCount(), context.getConfig().getMaxPlayers()),
                        "&1",
                        "&b  squareland.ru"
                );
                break;
            case DISABLED:
                scoreboardObjective.unsubscribeAll();
                break;
            case STARTING:
                if (startTimer % 20 == 0) {
                    for (User user : context.getUsers()) {
                        userService.sendTitle(user, format("&7Игра начнется через %s%s &7%s",
                                getTimerColor(startTimer / 20),
                                startTimer / 20,
                                StringUtils.plural((int) (startTimer / 20), "секунду", "секунды", "секунд")
                        ));
                    }
                }
                startTimer -= 1;
                scoreboardObjective.applyLines(
                        "&d Начало игры...",
                        "&1",
//                        format("&2 Игра начнется через &6%s %s", startTimer / 20, StringUtils.plural((int) (startTimer / 20), "секунду", "секунды", "секунд")),
//                        "&2",
                        "&b  squareland.ru"
                );
                break;
            case STOPPING:
                scoreboardObjective.applyLines(
                        "&d Окончание игры...",
                        "&1",
                        "&b  squareland.ru"
                );
                break;
            case STARTED:
                timer -= 1;
                scoreboardObjective.applyLines(
                        format("         &d%s", getTimerColor(timer / 20) + formatSeconds(timer / 20)),
                        "&1 ",
                        format("    &d#1 &7->&a %s &7(&a%s&7)",
                                userService.getTopUser().map(User::getUsername).orElse("----"),
                                userService.getTopUser().map(User::getLocalLevelsReached).orElse(0)),
                        "&b  squareland.ru"
                );
                if (timer <= 0) {
                    stop();
                }
                break;
        }


    }

    @Override
    public void join(@NonNull final User user) {
        Player player = user.asPlayer();
        if (context.getUsers().contains(user)) {
            return;
        }

        if (state == ArenaState.STOPPING || state == ArenaState.DISABLED) {
            return;
        }

        if (state == ArenaState.STARTED) {
            userService.sendTitle(user, "&aТеперь Вы наблюдатель");
            user.setDied(true);
        }
        else {
            user.setLocalLevelsReached(1);
            player.setLevel(1);
        }


        user.setCurrentArena(this);
        player.getInventory().clear();
        player.setExp(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        context.getUsers().add(user);
        context.getScoreboardObjective().subscribe(player);

        userService.broadcastMessage(format("&a + &d%s &7(&e%s&7/&c%s&7)", user.getUsername(), context.getUsersCount(), context.getConfig().getMaxPlayers()));
        if (context.getUsersCount() >= context.getConfig().getRequiredPlayersToStart()) {
            Promise.start()
                   .thenRunSync(() -> setStatus(ArenaState.STARTING))
                   .thenRunDelayedSync(this::start, context.getConfig().getArenaStartDelay().getSeconds(), TimeUnit.SECONDS)
                   .thenRunSync(() -> setStatus(ArenaState.STARTED));

        }
    }

    @Override
    public void leave(@NonNull User user, ArenaLeaveCause cause) {
        Player player = user.asPlayer();
        context.getScoreboardObjective().unsubscribe(player);
        user.setCurrentArena(null);
        user.setLevelsReached(0);
        player.setTotalExperience(0);
        user.setDied(false);
        player.getInventory().clear();
        userService.broadcastMessage(format("&a - &d%s &7(&e%s&7/&c%s&7)", user.getUsername(), context.getUsersCount(), context.getConfig().getMaxPlayers()));

    }

    @Override
    public <T extends AutoCloseable> @NonNull T bind(@NonNull final T terminable) {
        return compositeTerminable.bind(terminable);
    }

    @Override
    public void startListening() {
        subscribe(EntityDamageByEntityEvent.class)
                .filter(e -> (e.getDamager() instanceof Player) && (e.getEntity() instanceof Player) && state == ArenaState.STARTED)
                .handler(event -> {
                    Player attacker = (Player) event.getDamager();
                    Player victim = (Player) event.getEntity();
                    MetadataMap victimMeta = Metadata.provideForPlayer(victim);
                    victimMeta.forcePut(LAST_ATTACKER_KEY, ExpiringValue.of(attacker.getUniqueId(), 15, TimeUnit.SECONDS));
                })
                .bindWith(this);
        subscribe(ProjectileHitEvent.class)
                .filter(e -> e.getEntity() instanceof Arrow && e.getHitEntity() != null && e.getHitEntity() instanceof Player)
                .handler(event -> {
                    Arrow arrow = (Arrow) event.getEntity();
                    if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
                        Player attacker = (Player) arrow.getShooter();
                        Player victim = (Player) event.getHitEntity();
                        MetadataMap victimMeta = Metadata.provideForPlayer(victim);
                        victimMeta.forcePut(LAST_ATTACKER_KEY, ExpiringValue.of(attacker.getUniqueId(), 15, TimeUnit.SECONDS));
                    }
                })
                .bindWith(this);

        subscribe(PlayerDeathEvent.class)
                .filter(e -> userService.getUserByPlayer(e.getEntity()).flatMap(User::getCurrentArenaSafe).isPresent() && state == ArenaState.STARTED)
                .handler(event -> {
                    MetadataMap metadataMap = Metadata.provideForPlayer(event.getEntity());
                    Promise.start()
                           .thenApplySync($ -> userService.getUserByPlayerNullable(event.getEntity()))
                           .thenAcceptSync(user -> {
                               user.setDeaths(user.getDeaths() + 1);
                               user.setDied(true);
                           })
                           .thenApplySync($ -> metadataMap.getOrNull(LAST_ATTACKER_KEY))
                           .thenApplySync(Players::get)
                           .thenApplySync(userService::getUserByPlayer)
                           .thenAcceptAsync(user -> {
                               user.ifPresent(u -> {
                                   u.setKills(u.getKills() + 1);
                                   userService.sendBossBar(u,format("&6Вы убили %s", event.getEntity().getName()));
                                   u.asPlayer().giveExp(6);
                                   context.getUsers().forEach(us -> {
                                       userService.sendMessage(us, format("&a%s &6убит игроком &c%s", event.getEntity().getName(), u.getUsername()));
                                   });
                               });
                               if (!user.isPresent()) {
                                   context.getUsers().forEach(us -> userService.sendMessage(us, format("&a%s&e самоубился :(", event.getEntity().getName())));
                               }
                           })
                           .thenRunDelayedSync(() -> event.getEntity().spigot().respawn(), 3);
                })
                .bindWith(this);

        subscribe(PlayerRespawnEvent.class)
                .filter(e -> userService.getUserByPlayer(e.getPlayer()).flatMap(User::getCurrentArenaSafe).isPresent() && state == ArenaState.STARTED)
                .handler(event -> {
                    Position spawn = RandomSelector.uniform(userService.getUserByPlayerNullable(event.getPlayer()).getCurrentArena().getContext().getConfig().getPositions()).pick();
                    event.setRespawnLocation(spawn.toLocation());
                })
                .bindWith(this);

        subscribe(PlayerLevelChangeEvent.class)
                .filter(e -> userService.getUserByPlayer(e.getPlayer()).flatMap(User::getCurrentArenaSafe).isPresent() && state == ArenaState.STARTED)
                .handler(event -> {
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
                        userService.sendTitle(u,"&aLevelup!!!");
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 2), true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 1), true);
                        Players.spawnParticle(u.asPlayer().getEyeLocation(), Particle.VILLAGER_HAPPY);
                    });


                })
                .bindWith(this);
    }

    private String formatSeconds(long secs) {
        long millis = secs * 1000L;
        DateFormat df = new SimpleDateFormat("mm:ss");
        return df.format(millis);
    }

    private String getTimerColor(long timer) {
        if (timer >= 31 && timer <= 60) {
            return "&6";
        }
        else if (timer >= 0 && timer <= 30) {
            return "&c";
        }
        return "&a";
    }

    private String getStartTimerColor(long timer) {
        if (timer >= 11 && timer <= 15) {
            return "&a";
        }
        else if (timer >= 6 && timer <= 10) {
            return "&d";
        }
        return "&c";
    }


    @ToString
    @EqualsAndHashCode
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    public static class SingleArenaContext implements ArenaContext {

        @NonNull EquipmentResolver equipmentResolver;
        @NonNull Duration arenaStartDuration;
        @NonNull ArenaConfig config;
        @NonNull ScoreboardObjective scoreboardObjective;
        @NonNull Set<User> users;

        public SingleArenaContext(final ArenaConfig config, final @NonNull ScoreboardObjective scoreboardObjective, @NonNull final EquipmentResolver equipmentResolver) {
            this.config = config;
            this.scoreboardObjective = scoreboardObjective;
            this.arenaStartDuration = config.getArenaStartDelay();
            this.equipmentResolver = equipmentResolver;
            this.users = new LinkedHashSet<>(16); //pre-initialize set capacity to 16 (max possible users per arena)

        }


    }
}
