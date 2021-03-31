package com.pepej.gungame.repository.mapper;

import com.pepej.gungame.hologram.GunGameHologram;
import com.pepej.gungame.hologram.HologramType;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GunGameHologramRowMapper implements RowMapper<GunGameHologram> {
    @Override
    public GunGameHologram map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        double x = rs.getDouble("x");
        double y = rs.getDouble("y");
        double z = rs.getDouble("z");
        String world = rs.getString("world");
        HologramType type = HologramType.valueOf(rs.getString("type"));
        long updateInterval = rs.getLong("update_interval");
//        return new GunGameHologram(x,y,z,world,type, updateInterval);
        return null;
    }
}
