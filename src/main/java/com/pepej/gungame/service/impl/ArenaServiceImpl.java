package com.pepej.gungame.service.impl;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.service.ArenaService;
import com.pepej.papi.utils.Log;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArenaServiceImpl implements ArenaService {

    @Getter
    @NonNull
    Set<Arena> arenas;

    public ArenaServiceImpl() {
        this.arenas = new LinkedHashSet<>();
    }

    @Override
    public void register(@NonNull Arena arena) {
        if (arenas.stream().anyMatch(a -> a.getContext().getName().equals(arena.getContext().getName()))) {
            Log.warn("Arena with name " + arena.getContext().getName() + " already register!");
            return;
        }
        arenas.add(arena);
        arena.enable();
    }

    @Override
    public void unregister(@NonNull String name) {
        Optional<Arena> arena = arenas.stream()
                               .filter(a -> a.getContext().getName().equals(name))
                               .findFirst();

        arena.ifPresent(a -> {
            a.stop(Arena.ArenaStopCause.UNKNOWN);
            a.disable();
            arenas.remove(a);
        });
    }

    @Override
    public Optional<Arena> getArena(@NonNull String name) {
        return arenas.stream()
                     .filter(a -> a.getContext().getName().equals(name))
                     .findFirst();
    }

}
