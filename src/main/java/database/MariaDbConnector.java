package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MariaDbConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MariaDbConnector.class);
    private static MariaDbConfig config;
    static {
        try {
            config = new MariaDbConfig();
        } catch (IOException e) {
            LOGGER.error("Failed to load MariaDb config!", e);
        }
    }

    private MariaDbConnector() {
    }

    public static synchronized ResultSet executeSql(String sql) {
        if (config != null) {
            try (Connection conn = DriverManager.getConnection(
                config.getDbUrl(), config.getUser(), config.getPass());
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    return null;
                } else {
                    try (CachedRowSet rowset = RowSetProvider.newFactory()
                        .createCachedRowSet()) {
                        rowset.populate(rs);
                        return rowset;
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("Failed to query database", e);
            }
        }
        return null;
    }

    public static MariaDbConfig getConfig() {
        return config;
    }
}
