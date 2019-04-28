package commands.copy;

import java.util.HashMap;
import java.util.Map;

public final class CopyMessageChannelStorage {
    private static Map<String, String> channels = createMap();

    private CopyMessageChannelStorage() {
    }

    private static Map<String, String> createMap() {
        Map<String, String> channels = new HashMap<>();
        channels.put("421955449101746176", "512327719111884801");
        return channels;
    }

    public static Map<String, String> getChannelIDs() {
        return channels;
    }
}