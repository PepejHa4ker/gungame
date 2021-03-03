package com.pepej.gungame.arena.loader;

import com.pepej.gungame.api.Arena;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ArenaLoader {

    void saveArena(@NonNull Arena arena);

    @NonNull Arena loadArena(@NonNull String arena);

}
