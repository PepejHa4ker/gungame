package com.pepej.gungame.service;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.service.impl.ArenaServiceImpl;
import com.pepej.papi.services.Implementor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.Set;

@Implementor(ArenaServiceImpl.class)
public interface ArenaService {

    void register(@NonNull Arena arena);

    void unregister(@NonNull String name);

    @Nullable
    Arena getArenaNullable(@NonNull String id);

    @NonNull
    Optional<Arena> getArena(@NonNull String id);

    @NonNull
    Optional<Arena> getMostRelevantArena();

    @NonNull
    Set<Arena> getArenas();



}
