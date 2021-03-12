package com.pepej.gungame.repository;

import com.pepej.gungame.user.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.UUID;

public interface UserRepository {

    void createContainer();

    @NonNull
    List<Long> getAllIds();

    @NonNull
    List<User> getAllUsers();

    @Nullable
    User getUserById(long id);

    @Nullable
    User getUserByName(@NonNull final String userName);

    @Nullable
    User getUserByUuid(@NonNull final UUID uuid);

    void updateUser(@NonNull User user);

    void saveUser(@NonNull final User user);


}
