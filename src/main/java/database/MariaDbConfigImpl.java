package database;

import com.jagrosh.jdautilities.commons.ConfigLoader;
import com.typesafe.config.Config;

public final class MariaDbConfigImpl implements MariaDbConfig {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;
    private final String dbName;

    MariaDbConfigImpl() {
        Config config = ConfigLoader.getConfig();
        dbUrl = config.getString("dbUrl");
        dbUser = config.getString("dbUser");
        dbPass = config.getString("dbPass");
        dbName = config.getString("dbName");
    }

    @Override
    public String getDbUrl() {
        return dbUrl;
    }

    @Override
    public String getDbUser() {
        return dbUser;
    }

    @Override
    public String getDbPass() {
        return dbPass;
    }

    @Override
    public String getDbName() {
        return dbName;
    }
}