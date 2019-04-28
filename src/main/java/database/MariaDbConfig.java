package database;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import utils.TxtReader;

final class MariaDbConfig {
    private static final URL CONFIG_FILE = MariaDbConfig.class.getResource("/configuration/mariadb.txt");
    private final String dbUrl;
    private final String user;
    private final String pass;
    private final String dbName;

    MariaDbConfig() throws IOException {
        TxtReader txtReader = new TxtReader(CONFIG_FILE);
        List<String> list = txtReader.readTxtFile();
        dbUrl = list.get(0);
        user = list.get(1);
        pass = list.get(2);
        dbName = list.get(3);
    }

    String getDbUrl() {
        return dbUrl;
    }

    String getUser() {
        return user;
    }

    String getPass() {
        return pass;
    }

    String getDbName() {
        return dbName;
    }
}
