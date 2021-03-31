package com.pepej.gungame.repository.mysql;

import com.pepej.gungame.hologram.GunGameHologram;
import com.pepej.gungame.hologram.HologramType;
import com.pepej.gungame.repository.HologramRepository;
import com.pepej.gungame.repository.mapper.GunGameHologramRowMapper;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface MySQLHologramRepository extends HologramRepository {

    @Override
    @SqlQuery("SELECT userId FROM gg_holograms")
    List<String> getAllIds();

    @Override
    @RegisterRowMapper(GunGameHologramRowMapper.class)
    @SqlQuery("SELECT * FROM gg_holograms")
    List<GunGameHologram> getAllHolograms();

    @Override
    @RegisterRowMapper(GunGameHologramRowMapper.class)
    @SqlQuery("SELECT * FROM gg_holograms WHERE userId = :userId")
    @Nullable GunGameHologram getHologramByUserId(@Bind("userId") String userId);

    @Override
    @SqlUpdate("INSERT IGNORE INTO gg_holograms(userId, x, y, z, world, type, update_interval) VALUES (:userId, :x, :y, :z, :world, :type, :updateInterval) ")
    void saveHologram(
            @Bind("userId") String userId,
            @Bind("x") double x,
            @Bind("y") double y,
            @Bind("z") double z,
            @Bind("world") String world,
            @Bind("type") HologramType type,
            @Bind("updateInterval") long updateInterval
    );

    @SqlUpdate("UPDATE gg_holograms SET x = :x, y = :y, z = :z, world = :world, type = :type, update_interval = :updateInterval WHERE userId = :userId")
    void updateHologram(
            @Bind("userId") String userId,
            @Bind("x") double x,
            @Bind("y") double y,
            @Bind("z") double z,
            @Bind("world") String world,
            @Bind("type") HologramType type,
            @Bind("updateInterval") long updateInterval
    );
}
