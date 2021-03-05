package com.pepej.gungame.arena;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.user.User;
import com.pepej.papi.adventure.text.Component;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.scoreboard.ScoreboardObjective;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.composite.CompositeTerminable;
import com.pepej.papi.utils.Log;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashSet;
import java.util.Set;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SingleArena implements Arena, TerminableConsumer {

    @Getter
    @NonNull World world;
    @Getter
    @NonNull ArenaContext context;
    @NonNull CompositeTerminable compositeTerminable;

    @Getter
    @NonNull
    @NonFinal
    ArenaState state;


    public SingleArena(final @NonNull World world, final @NonNull ArenaContext context) {
        this.world = world;
        this.context = context;
        this.state = ArenaState.DISABLED;
        this.compositeTerminable = CompositeTerminable.create();

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
        setStatus(ArenaState.ENABLED);
        Schedulers.builder()
                  .async()
                  .every(1)
                  .run(this)
                  .bindWith(this);
    }


    @Override
    public void disable() {
        compositeTerminable.closeAndReportException();
        setStatus(ArenaState.DISABLED);
    }

    @Override
    public void start() {

    }

    @Override
    public void start(final long afterTics) {

    }

    @Override
    public void stop(final ArenaStopCause cause) {

    }

    @Override
    public void stop(final ArenaStopCause cause, final long afterTics) {

    }

    @Override
    public void selectTeam(@NonNull final User user, final Team team) {

    }

    @Override
    public void run() {

    }

    @Override
    public void join(@NonNull final User user) {
        user.setCurrentArena(this);
        user.sendMessage(Component.text("Joined arena " + context.getConfig().getArenaName()));
        Player player = user.asPlayer();
        context.getScoreboardObjective().subscribe(player);

    }

    @Override
    public void leave(@NonNull User user) {
        user.sendMessage(Component.text("Leave arena " + context.getConfig().getArenaName()));
        Player player = user.asPlayer();
        context.getScoreboardObjective().unsubscribe(player);
        user.setCurrentArena(null);

    }

    @Override
    public <T extends AutoCloseable> @NonNull T bind(@NonNull final T terminable) {
        return this.compositeTerminable.bind(terminable);
    }


    @ToString
    @EqualsAndHashCode
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class SingleArenaContext implements ArenaContext {

        @Getter
        ArenaConfig config;
        @Getter
        @NonNull ScoreboardObjective scoreboardObjective;
        @Getter
        @NonNull Set<Team> teams;
        @Getter
        @NonNull Set<User> users;

        public SingleArenaContext(final ArenaConfig config, final @NonNull ScoreboardObjective scoreboardObjective) {
            this.config = config;
            this.scoreboardObjective = scoreboardObjective;

            this.teams = new LinkedHashSet<>(5);
            this.users = new LinkedHashSet<>(16);

        }


    }
}
