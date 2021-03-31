package com.pepej.gungame.arena.loader;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.arena.SingleArena;
import com.pepej.gungame.equipment.EquipmentResolver;
import com.pepej.gungame.equipment.EquipmentResolverImpl;
import com.pepej.gungame.service.ScoreboardService;
import com.pepej.papi.Papi;
import com.pepej.papi.config.ConfigFactory;
import com.pepej.papi.config.ConfigurationNode;
import com.pepej.papi.scoreboard.Scoreboard;
import com.pepej.papi.scoreboard.ScoreboardProvider;
import com.pepej.papi.services.Services;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.Objects;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimpleArenaLoader implements ArenaLoader {

    @NonNull File file;

    public SimpleArenaLoader(@NonNull File file) {
        this.file = file;
    }

    @Override
    public void saveArena(@NonNull final Arena arena) {
        ArenaConfig config = arena.getContext().getConfig();
        saveDataToFile(config);
    }

    @SneakyThrows
    private void saveDataToFile(ArenaConfig config) {
        ConfigurationNode arenaDataNode = ConfigFactory.gson().load(file);
        arenaDataNode.set(ArenaConfig.class, config);
        ConfigFactory.gson().loader(file).save(arenaDataNode);
    }

    @Override
    @NonNull
    @SneakyThrows
    public Arena loadArena(@NonNull final String arena) {
        ArenaConfig arenaConfig = ConfigFactory.gson()
                                               .load(file)
                                               .node(arena)
                                               .get(ArenaConfig.class);

        if (arenaConfig == null) {
            throw new NullPointerException("Arena file not found!");
        }
        EquipmentResolver equipmentResolver = new EquipmentResolverImpl();
        ScoreboardProvider provider = Services.load(ScoreboardProvider.class);
        ScoreboardService scoreboardService = Services.load(ScoreboardService.class);
        Scoreboard scoreboard = provider.getScoreboard();
        scoreboardService.register(arena, scoreboard);
        World world = Papi.worldNullable(arenaConfig.getArenaWorld());
        Objects.requireNonNull(world, "world");
        return new SingleArena(world, new SingleArena.SingleArenaContext(arenaConfig, scoreboard, equipmentResolver, null));
    }
}
