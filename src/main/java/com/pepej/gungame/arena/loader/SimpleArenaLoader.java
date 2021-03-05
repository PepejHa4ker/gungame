package com.pepej.gungame.arena.loader;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.arena.SingleArena;
import com.pepej.gungame.service.TeamService;
import com.pepej.papi.Papi;
import com.pepej.papi.Services;
import com.pepej.papi.config.ConfigFactory;
import com.pepej.papi.scoreboard.ScoreboardObjective;
import com.pepej.papi.scoreboard.ScoreboardProvider;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import ninja.leaping.configurate.ConfigurationNode;
import org.bukkit.World;
import org.bukkit.scoreboard.DisplaySlot;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimpleArenaLoader implements ArenaLoader {

    @NonNull File file;
    public SimpleArenaLoader(@NotNull File file) {
        this.file = file;
    }

    @Override
    public void saveArena(@NonNull final Arena arena) {
        ArenaConfig config = ArenaConfig
                .builder()
                .arenaWorld(arena.getWorld().getName())
                .arenaName(arena.getContext().getName())
                .lobby(arena.getContext().getLobby())
                .build();

        saveDataToFile(config);
    }

    @SneakyThrows
    private void saveDataToFile(ArenaConfig config) {
        ConfigurationNode arenaDataNode = ConfigFactory.gson().load(file);
        arenaDataNode.setValue(ArenaConfig.TOKEN, config);
        ConfigFactory.gson().loader(file).save(arenaDataNode);
    }

    @Override
    @NonNull
    @SneakyThrows
    public Arena loadArena(@NonNull final String arena) {
        ArenaConfig arenaData = ConfigFactory.gson()
                                             .load(file)
                                             .getNode(arena)
                                             .getValue(ArenaConfig.TOKEN);

        if (arenaData == null) {
            throw new NullPointerException("Arena file not found!");
        }
        ScoreboardProvider provider = Services.load(ScoreboardProvider.class);

        ScoreboardObjective objective = provider.getScoreboard().createObjective(arena, "&bGunGame", DisplaySlot.SIDEBAR, false);
        World world = Papi.worldNullable(arenaData.getArenaWorld());
        Objects.requireNonNull(world, "world");
        return new SingleArena(world,
                new SingleArena.SingleArenaContext(
                        arenaData.getArenaName(),
                        objective,
                        arenaData.getLobby()
                ));
    }
}
