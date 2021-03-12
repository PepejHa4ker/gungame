package com.pepej.gungame.service;

import com.pepej.gungame.service.impl.UserServiceImpl;
import com.pepej.gungame.user.User;
import com.pepej.papi.adventure.text.Component;
import com.pepej.papi.adventure.title.Title;
import com.pepej.papi.services.Implementor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.pepej.papi.text.Text.colorize;

@Implementor(UserServiceImpl.class)
public interface UserService {

    void loadAllOnlineUsers();

    void broadcastMessage(@NonNull Component message);

    void sendMessage(@NonNull User user, @NonNull Component message);

    default void sendMessage(@NonNull User user, @NonNull String message) {
        sendMessage(user, Component.text(colorize(message)));
    }

    default void broadcastMessage(@NonNull String message) {
        broadcastMessage(Component.text(colorize(message)));
    }

     void sendBossBar(@NonNull User user, @NonNull Component message);

    default void sendBossBar(@NonNull User user, @NonNull String message) {
        sendBossBar(user, Component.text(colorize(message)));
    }

    void sendTitle(@NonNull User user, @NonNull Title title);

    default void sendTitle(@NonNull User user, @NonNull String message) {
        Title title = Title.title(Component.text(colorize(message)), Component.empty());
        sendTitle(user, title);
    }


    void registerUser(@NonNull UUID id, String username);

    void unregisterUser(@NonNull UUID id);

    @Nullable
    User getUserByPlayerNullable(@NonNull Player player);

    @NonNull
    Optional<User> getUserByPlayer(@NonNull Player player);

    @NonNull
    Optional<User> getUserByPlayer(@NonNull Optional<Player> player);

    @Nullable
    User getUserNullable(@NonNull UUID id);

    Optional<User> getUser(@NonNull UUID id);

    @Nullable
    User getTopUserNullable();

    @NonNull
    default Optional<User> getTopUser() {
        return Optional.ofNullable(getTopUserNullable());
    }

    Set<User> getAllUsers();

}
