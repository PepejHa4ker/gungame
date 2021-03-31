package com.pepej.gungame.repository;

import com.pepej.papi.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.File;

@Getter
public class DatabaseService {


    private final Jdbi jdbi;
    private final HikariDataSource hikariDataSource;


    @SneakyThrows
    public DatabaseService(File file) {
        DatabaseConfig config = ConfigFactory.gson()
                                             .load(file)
                                             .get(DatabaseConfig.class);
        HikariConfig hConfig = new HikariConfig();
        hConfig.setPoolName("GunGame MySQL Connection pool");
        hConfig.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hConfig.addDataSourceProperty("serverName", config.getAddress());
        hConfig.addDataSourceProperty("port", config.getPort());
        hConfig.addDataSourceProperty("databaseName", config.getDatabaseName());
        hConfig.addDataSourceProperty("user", config.getUser());
        hConfig.addDataSourceProperty("password", config.getPassword());
        hConfig.addDataSourceProperty("useSSL", false);
        hikariDataSource = new HikariDataSource(hConfig);
        jdbi = Jdbi.create(hikariDataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
    }
}
