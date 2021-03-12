package com.pepej.gungame.api;

import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.equipment.EquipmentResolver;
import com.pepej.gungame.user.User;
import com.pepej.papi.scoreboard.ScoreboardObjective;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.Set;

public interface Arena extends Runnable {

    default boolean actualToJoin() {
        return this.getState() == ArenaState.STARTING || this.getState() == ArenaState.WAITING;
    }

    void enable();

    void disable();

    void start();

    void stop();

    void resetTimers();

    void join(@NonNull User user);

    void leave(@NonNull User user, ArenaLeaveCause cause);

    void startListening();

    @NonNull
    ArenaContext getContext();

    @NonNull
    World getWorld();

    @NonNull
    ArenaState getState();

    interface ArenaContext {

        @NonNull EquipmentResolver getEquipmentResolver();

        @NonNull Duration getArenaStartDuration();

        @NonNull ArenaConfig getConfig();

        @NonNull ScoreboardObjective getScoreboardObjective();

        @NonNull Set<User> getUsers();

        default int getUsersCount() {
            return this.getUsers().size();
        }

    }

    enum ArenaState {
        WAITING,
        DISABLED,
        STARTING,
        STOPPING,
        STARTED;

    }

    enum ArenaLeaveCause {
        END_OF_GAME,
        FORCE
    }
}
