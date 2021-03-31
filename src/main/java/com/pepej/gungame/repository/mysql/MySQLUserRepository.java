package com.pepej.gungame.repository.mysql;

import com.pepej.gungame.repository.UserRepository;
import com.pepej.gungame.repository.mapper.UserRowMapper;
import com.pepej.gungame.user.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface MySQLUserRepository extends UserRepository {

    @Override
    @SqlQuery("SELECT id FROM gg_users")
    @Unmodifiable
    @NonNull List<Long> getAllIds();

    @Override
    @SqlQuery("SELECT * FROM gg_users")
    @Unmodifiable
    @NonNull List<User> getAllUsers();


    @SqlQuery("SELECT * FROM gg_users WHERE user_id = :uuid")
    @RegisterRowMapper(UserRowMapper.class)
    @Unmodifiable
    @Nullable User getUserByUuid(final @NonNull @Bind("uuid") String uuid);

    @Override
    @SqlQuery("SELECT * FROM gg_users ORDER BY kills DESC LIMIT :size")
    @RegisterRowMapper(UserRowMapper.class)
    @Unmodifiable
    @NonNull List<@Nullable User> getTopUsersByKills(@Bind("size") long size);

    @Override
    @SqlQuery("SELECT * FROM gg_users ORDER BY levels_reached DESC LIMIT :size")
    @RegisterRowMapper(UserRowMapper.class)
    @Unmodifiable
    @NonNull List<@Nullable User> getTopUsersByLevels(@Bind("size") long size);

    @Override
    @SqlQuery("SELECT * FROM gg_users ORDER BY games DESC LIMIT :size")
    @RegisterRowMapper(UserRowMapper.class)
    @Unmodifiable
    @NonNull List<@Nullable User> getTopUsersByGames(@Bind("size") long size);

    @Override
    @SqlQuery("SELECT * FROM gg_users ORDER BY wins DESC LIMIT :size")
    @RegisterRowMapper(UserRowMapper.class)
    @Unmodifiable
    @NonNull List<@Nullable User> getTopUsersByWins(@Bind("size") long size);

    @Override
    @SqlQuery("SELECT * FROM gg_users ORDER BY exp DESC LIMIT :size")
    @RegisterRowMapper(UserRowMapper.class)
    @Unmodifiable
    @NonNull List<@Nullable User> getTopUsersByExp(@Bind("size") long size);

    @Override
    @SqlUpdate(
            "UPDATE gg_users " +
                    " SET games = :gamesPlayed," +
                    " wins = :wins," +
                    " kills = :kills," +
                    " deaths = :deaths," +
                    " levels_reached = :levelsReached," +
                    " exp = :exp" +
                    " WHERE user_id = :id")
    void updateUser(
            @Bind("id") String id,
            @Bind("gamesPlayed") int gamesPlayed,
            @Bind("wins") int wins,
            @Bind("kills") int kills,
            @Bind("deaths") int deaths,
            @Bind("levelsReached") int levelsReached,
            @Bind("exp") int exp);


    @Override
    @SqlQuery("SELECT EXISTS(SELECT id FROM gg_users WHERE user_id = :uuid)")
    boolean userExists(final @NonNull @Bind("uuid") String uuid);

    @Override
    @RegisterBeanMapper(User.class)
    @SqlUpdate("INSERT IGNORE INTO gg_users(user_id, username) VALUES (:id, :username)")
    void saveUser(final @NonNull @Bind("id") String id, @Bind("username") String username);
}
