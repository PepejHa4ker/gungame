package com.pepej.gungame.api;

import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.user.User;
import com.pepej.papi.scoreboard.ScoreboardObjective;
import org.bukkit.World;
import org.bukkit.scoreboard.Team;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

public interface Arena extends Runnable {

    default boolean actualToJoin() {
        return this.getState() == ArenaState.STARTING || this.getState() == ArenaState.ENABLED;
    }

    void enable();

    void disable();

    void start();

    void start(long afterTics);

    void stop(ArenaStopCause cause);

    void stop(ArenaStopCause cause, long afterTics);

    void selectTeam(@NonNull User user, Team team);

    void join(@NonNull User user);

    void leave(@NonNull User user);

    @NonNull
    ArenaContext getContext();

    @NonNull
    World getWorld();

    @NonNull
    ArenaState getState();

    interface ArenaContext {

        @NonNull ArenaConfig getConfig();

        @NonNull ScoreboardObjective getScoreboardObjective();

        @NonNull Set<User> getUsers();

    }

    enum ArenaState {
        ENABLED,
        DISABLED,
        STARTING,
        STOPPING,
        STARTED;

    }

    enum ArenaStopCause {
        UNKNOWN;
    }

}
