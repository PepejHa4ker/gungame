package com.pepej.gungame.repository;

import com.pepej.gungame.hologram.GunGameHologram;
import com.pepej.gungame.hologram.HologramType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface HologramRepository {


    @SqlQuery("SELECT userId FROM gg_holograms")
    List<String> getAllIds();

    List<GunGameHologram> getAllHolograms();

    @Nullable
    GunGameHologram getHologramByUserId(String userId);

    void saveHologram(String userId, double x, double y, double z, String world, HologramType type, long updateInterval);

    void updateHologram(String userId, double x, double y, double z, String world, HologramType type, long updateInterval);
}
