package com.pepej.gungame.service.impl;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.service.ArenaService;
import com.pepej.papi.utils.Log;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArenaServiceImpl implements ArenaService {

    @Getter
    @NonNull
    Set<Arena> arenas;

    public ArenaServiceImpl() {
        this.arenas = new HashSet<>();
    }

    @Override
    public void register(@NonNull Arena arena) {
        if (arenas.stream().anyMatch(a -> a.getContext().getConfig().getArenaName().equals(arena.getContext().getConfig().getArenaName()))) {
            Log.warn("Arena with name " + arena.getContext().getConfig().getArenaName() + " already register!");
            return;
        }
        arenas.add(arena);
        arena.enable();
    }

    @Override
    public void unregister(@NonNull String name) {
        Optional<Arena> arena = arenas.stream()
                               .filter(a -> a.getContext().getConfig().getArenaName().equals(name))
                               .findFirst();

        arena.ifPresent(a -> {
            a.stop();
            a.disable();
            arenas.remove(a);
        });
    }

    @Nullable
    @Override
    public Arena getArenaNullable(@NonNull final String name) {
        return getArena(name).orElse(null);
    }

    @NonNull
    @Override
    public Optional<Arena> getArena(@NonNull String name) {
        return arenas.stream()
                     .filter(a -> a.getContext().getConfig().getArenaName().equals(name))
                     .findFirst();
    }

    @NonNull
    @Override
    public Optional<Arena> getMostRelevantArena() {
        return arenas.stream()
                     .filter(Arena::actualToJoin)
                     .min((a1, a2) -> a2.getContext().getUsers().size() - a1.getContext().getUsers().size());
    }

}
