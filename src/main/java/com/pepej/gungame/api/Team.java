package com.pepej.gungame.api;

import com.pepej.gungame.user.User;
import com.pepej.papi.serialize.Point;
import org.bukkit.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

public interface Team {
    default int getSize() {
        return getUsers().size();
    }

    int getMaxSize();

    default boolean canJoin(@NonNull User user) {
        return getMaxSize() > getSize();
    }

    default void join(@NonNull User user) {
        getUsers().add(user);

//        Events.callSync(new TeamJoinEvent(user, this));
    }

    default void leave(@NonNull User user) {
        getUsers().remove(user);

//        Events.callSync(new TeamLeaveEvent(player, this));
    }

    @NonNull Set<Point> getSpawns();

    @NonNull Point getNextSpawn();

    @NonNull
    Set<User> getUsers();

    @NonNull TeamContext getContext();

    interface TeamContext {

        /**
         * @return название команды
         */
        @NonNull String getName();

        /**
         * @return цвет команды
         */
        @NonNull Color getColor();


    }


}
