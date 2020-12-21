package commands.channel;

public final class ChannelInfo {
    private final String name;
    private final String description;
    private final boolean storeInDatabase;

    public ChannelInfo(String name, String description, boolean storeInDatabase) {
        this.name = name;
        this.description = description;
        this.storeInDatabase = storeInDatabase;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean getStoreInDatabase() {
        return storeInDatabase;
    }
}