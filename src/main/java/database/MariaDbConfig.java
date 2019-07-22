package database;

public interface MariaDbConfig {
    String getDbUrl();

    String getDbUser();

    String getDbPass();

    String getDbName();
}
