package com.pepej.gungame.api;

import com.pepej.gungame.user.User;
import com.pepej.papi.serialize.Point;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

public interface Arena {

    void enable();

    void disable();

    void start();

    void start(long afterTics);

    void stop(ArenaStopCause cause);

    void stop(ArenaStopCause cause, long afterTics);

    @NonNull
    Team selectRandomTeam(@NonNull User user);

    void selectTeam(@NonNull User user, Team team);

    void join(@NonNull User user);

    @NonNull
    ArenaContext getContext();

    @NonNull
    World getWorld();

    @NonNull
    ArenaState getState();

    interface ArenaContext {

        @NonNull String getName();

        default int getTeamsAmount() {
            return getTeams().size();
        }

        @NonNull Set<Team> getTeams();

        @NonNull Set<User> getUsers();

        @NonNull Point getLobby();
    }

    enum ArenaState {
        ENABLED,
        DISABLED,
        STARTING,
        STOPPING,
        STARTED;

    }

    enum ArenaStopCause {
        INNOCENT_WIN,
        IMPOSTER_WIN,
        UNKNOWN;
    }

}
