package com.pepej.gungame.api;

import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.equipment.EquipmentResolver;
import com.pepej.gungame.rpg.trap.Trap;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.scoreboard.Scoreboard;
import com.pepej.papi.scoreboard.ScoreboardObjective;
import com.pepej.papi.serialize.Point;
import com.pepej.papi.serialize.Region;
import com.pepej.papi.terminable.TerminableConsumer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.event.player.PlayerEvent;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public interface Arena extends Runnable, TerminableConsumer {

    default boolean actualToJoin() {
        return this.getContext().getUsersCount() < this.getContext().getConfig().getMaxPlayers() &&
                this.getState() != ArenaState.DISABLED;
    }

    void updateMemberScoreboard(User user, ScoreboardObjective objective);

    void enable();

    void disable();

    void start();

    void stop();

    void resetTimers();

    void join(@NonNull User user, ArenaJoinType joinType);

    void leave(@NonNull User user, ArenaLeaveCause cause);

    void startListening();

    @NonNull
    ArenaContext getContext();

    @NonNull
    World getWorld();

    Point getRandomPositionToSpawn();

    @NonNull
    ArenaState getState();


    interface ArenaContext {

        @NonNull EquipmentResolver getEquipmentResolver();

        @NonNull Duration getArenaStartDuration();

        @NonNull ArenaConfig getConfig();

        void setConfig(@NonNull ArenaConfig arenaConfig);

        @NonNull Scoreboard getScoreboard();

        @NonNull Set<User> getUsers();

        @NonNull Map<Region, Trap> getTraps();

        @NonNegative
        int getMaxUserLevel();

        @NonNegative
        default int getUsersCount() {
            return this.getUsers().size();
        }

    }

    @AllArgsConstructor
    @Getter
    enum ArenaJoinType {
        SPECTATOR,
        MEMBER
    }

    @AllArgsConstructor
    @Getter
    enum ArenaState {
        WAITING("Ожидание игроков"),
        DISABLED("Оффлайн"),
        STARTING("Игра начинается"),
        STOPPING("Перезагрузка"),
        STARTED("В игре");

        String desc;


    }

    enum ArenaLeaveCause {
        END_OF_GAME,
        FORCE
    }
}
