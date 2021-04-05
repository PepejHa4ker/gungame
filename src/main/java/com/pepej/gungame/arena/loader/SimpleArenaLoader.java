package com.pepej.gungame.arena.loader;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.arena.SingleArena;
import com.pepej.gungame.equipment.EquipmentResolver;
import com.pepej.gungame.service.ArenaService;
import com.pepej.gungame.service.ScoreboardService;
import com.pepej.papi.Papi;
import com.pepej.papi.config.ConfigFactory;
import com.pepej.papi.config.ConfigurationNode;
import com.pepej.papi.scoreboard.Scoreboard;
import com.pepej.papi.scoreboard.ScoreboardProvider;
import com.pepej.papi.services.Service;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimpleArenaLoader implements ArenaLoader {

    @NonNull File file;

    @Service
    @NonFinal
    ArenaService arenaService;

    @Service
    @NonFinal
    EquipmentResolver equipmentResolver;

    @Service
    @NonFinal
    ScoreboardProvider scoreboardProvider;

    @Service
    @NonFinal
    ScoreboardService scoreboardService;

    public SimpleArenaLoader(@NonNull File file) {
        this.file = file;
    }


    @SneakyThrows
    @Override
    public void loadAndSaveArenaFromConfig(@NonNull final ArenaConfig config) {
        final ConfigurationNode node = ConfigFactory.gson().load(file);
        final List<ArenaConfig> arenaConfigs = node.getList(ArenaConfig.class);
        if (arenaConfigs == null) {
            return;
        }
        arenaConfigs.add(config);
        node.setList(ArenaConfig.class, arenaConfigs);
        ConfigFactory.gson().save(file, node);
        registerArenaFromConfig(config);

    }


    @SneakyThrows
    @Override
    public void loadAndRegisterAllArenas() {
        List<ArenaConfig> arenaConfigs = ConfigFactory.gson()
                                                      .load(file)
                                                      .getList(ArenaConfig.class);
        if (arenaConfigs == null) {
            throw new NullPointerException("Arena file not found!");
        }
        for (ArenaConfig arenaConfig : arenaConfigs) {
            registerArenaFromConfig(arenaConfig);
        }

    }

    private void registerArenaFromConfig(@NonNull ArenaConfig config) {
        Scoreboard scoreboard = scoreboardProvider.getScoreboard();
        scoreboardService.register(config.getArenaId(), scoreboard);
        World world = Papi.worldNullable(config.getArenaWorld());
        Objects.requireNonNull(world, "world");
        final Arena arena = new SingleArena(world, new SingleArena.SingleArenaContext(config, scoreboard, equipmentResolver, null));
        arenaService.register(arena);
    }
}
