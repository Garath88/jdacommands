package commands.channel.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.MariaDbConnector;

public final class SqlUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlUtil.class);

    private SqlUtil() {

    }

    static void executeQuery(String sql) {
        try {
            LOGGER.debug(sql);
            ResultSet result = MariaDbConnector.executeSql(sql);
            if (result != null) {
                result.close();
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to close resource from query", e);
        }
    }
}
