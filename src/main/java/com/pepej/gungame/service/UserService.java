package com.pepej.gungame.service;

import com.pepej.gungame.user.User;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserService {

    void loadAllOnlineUsers();

    void registerUser(@NonNull UUID id, String username);

    void unregisterUser(@NonNull UUID id);

    @Nullable
    User getUserByPlayerNullable(@NonNull Player player);

    @NonNull
    Optional<User> getUserByPlayer(@NonNull Player player);

    @Nullable
    User getUserNullable(@NonNull UUID id);

    Optional<User> getUser(@NonNull UUID id);

    Set<User> getAllUsers();

}
