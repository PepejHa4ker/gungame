package com.pepej.gungame.repository;

import com.pepej.gungame.user.User;
import com.pepej.papi.promise.Promise;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public interface UserRepository {

    @NonNull
    List<Long> getAllIds();

    @NonNull
    default Promise<List<Long>> getAllIdsAsync() {
        return Promise.supplyingAsync(this::getAllIds);
    }

    @NonNull
    List<@Nullable User> getAllUsers();

    @NonNull
    default Promise<@NonNull List<@Nullable User>> getAllUsersAsync() {
        return Promise.supplyingAsync(this::getAllUsers);
    }

    @Nullable
    User getUserByUuid(@NonNull final String uuid);

    @NonNull
    default Promise<@Nullable User> getUserByUuidAsync(@NonNull final String uuid) {
        return Promise.supplyingAsync(() -> getUserByUuid(uuid));
    }

    @NonNull
    List<@Nullable User> getTopUsersByKills(final long size);

    @NonNull
    default Promise<@NonNull List<@Nullable User>> getTopUsersByKillsAsync(final long size) {
        return Promise.supplyingAsync(() -> getTopUsersByKills(size));
    }

    @NonNull
    List<@Nullable User> getTopUsersByLevels(final long size);

    @NonNull
    default Promise<@NonNull List<@Nullable User>> getTopUsersByLevelsAsync(final long size) {
        return Promise.supplyingAsync(() -> getTopUsersByLevels(size));
    }

    @NonNull
    List<@Nullable User> getTopUsersByGames(final long size);

    @NonNull
    default Promise<@NonNull List<@Nullable User>> getTopUsersByGamesAsync(final long size) {
        return Promise.supplyingAsync(() -> getTopUsersByGames(size));
    }

    @NonNull
    List<@Nullable User> getTopUsersByWins(final long size);

    @NonNull
    default Promise<@NonNull List<@Nullable User>> getTopUsersByWinsAsync(final long size) {
        return Promise.supplyingAsync(() -> getTopUsersByWins(size));
    }

    @NonNull
    List<@Nullable User> getTopUsersByExp(final long size);

    @NonNull
    default Promise<@NonNull List<@Nullable User>> getTopUsersByExpAsync(final long size) {
        return Promise.supplyingAsync(() -> getTopUsersByExp(size));
    }

    void updateUser(
            String id,
            int gamesPlayed,
            int wins,
            int kills,
            int deaths,
            int levelsReached,
            int exp

    );


    void saveUser(final @NonNull String id, String username);

    boolean userExists(@NonNull final String uuid);

    @NonNull
    default Promise<@NonNull Boolean> userExistsAsync(@NonNull final String uuid) {
        return Promise.supplyingAsync(() -> userExists(uuid));
    }


}
