package com.punish.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jdbi.v3.core.Jdbi;

import com.punish.Model.Enums.BracketType;
import com.punish.Model.Enums.MatchStatus;
import com.punish.Model.Enums.TournamentStatus;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database {
    private static Jdbi jdbi;
    private static HikariDataSource datasSource;

    public static Jdbi getJdbi() {
        if (jdbi == null) {
            jdbi = Jdbi.create(getDataSource());
            jdbi.registerColumnMapper(TournamentStatus.class, new TournamentStatusMapper());
            jdbi.registerColumnMapper(BracketType.class, (rs, col, ctx) -> BracketType.valueOf(rs.getString(col)));
            jdbi.registerColumnMapper(MatchStatus.class, (rs, col, ctx) -> MatchStatus.valueOf(rs.getString(col)));
        }
        return jdbi;
    }

    public static Properties loadProperties(){
        Properties props = new Properties();
        try (InputStream input = Database.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (System.getenv("DB_URL") != null) props.setProperty("db.url", System.getenv("DB_URL"));
        if (System.getenv("DB_USER") != null) props.setProperty("db.user", System.getenv("DB_USER"));
        if (System.getenv("DB_PASSWORD") != null) props.setProperty("db.password", System.getenv("DB_PASSWORD"));
        if (System.getenv("JWT_SECRET") != null) props.setProperty("jwt.secret", System.getenv("JWT_SECRET"));
        return props;
    }

    public static HikariDataSource getDataSource(){
        if (datasSource == null) {
            Properties props = Database.loadProperties();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));

            datasSource = new HikariDataSource(config);
        }
        return datasSource;
    }
}
