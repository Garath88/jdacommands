package commands.thread;

public final class ThreadInfo {
    private final String name;
    private final String description;
    private final boolean storeInDatabase;

    public ThreadInfo(String name, String description, boolean storeInDatabase) {
        this.name = name;
        this.description = description;
        this.storeInDatabase = storeInDatabase;
    }

    public String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    boolean getStoreInDatabase() {
        return storeInDatabase;
    }
}