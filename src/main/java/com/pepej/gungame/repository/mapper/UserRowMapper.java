package com.pepej.gungame.repository.mapper;


import com.pepej.gungame.user.User;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRowMapper implements RowMapper<User> {


    @Override
    public User map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        UUID uid = UUID.fromString(rs.getString("user_id"));
        String username = rs.getString("username");
        int gamesPlayed = rs.getInt("games");
        int wins = rs.getInt("wins");
        int kills = rs.getInt("kills");
        int deaths = rs.getInt("deaths");
        int levelsReached = rs.getInt("levels_reached");
        int exp = rs.getInt("exp");
        User user = new User(uid, username);
        user.setGamesPlayed(gamesPlayed);
        user.setWins(wins);
        user.setKills(kills);
        user.setDeaths(deaths);
        user.setLevelsReached(levelsReached);
        user.setExp(exp);
        return user;
    }
}
