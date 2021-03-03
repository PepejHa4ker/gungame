package com.pepej.gungame.service;

import com.pepej.gungame.api.Arena;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.Set;

public interface ArenaService {

    void register(@NonNull Arena arena);

    void unregister(@NonNull String name);

    Optional<Arena> getArena(@NonNull String name);

    Optional<Arena> getMostRelevantArena();

    @NonNull
    Set<Arena> getArenas();

}
