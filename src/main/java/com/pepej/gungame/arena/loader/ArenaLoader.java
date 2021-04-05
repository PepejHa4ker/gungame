package com.pepej.gungame.arena.loader;

import com.pepej.gungame.arena.ArenaConfig;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ArenaLoader {

    void loadAndSaveArenaFromConfig(@NonNull ArenaConfig config);


    void loadAndRegisterAllArenas();


}
