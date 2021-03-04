package com.pepej.gungame.service;

import com.pepej.gungame.user.User;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserService {

    void registerUser(@NonNull UUID id, String username);

    void unregisterUser(@NonNull UUID id);

    Optional<User> getUser(@NonNull UUID id);

    Set<User> getAllUsers();

}
