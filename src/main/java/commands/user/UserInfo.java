package commands.user;

import java.util.ArrayList;
import java.util.List;

public final class UserInfo {
    private final String id;
    private List<String> bannedEmojis = new ArrayList<>();

    UserInfo(String id) {
        this.id = id;
    }

    void addEmoji(String emoji) {
        bannedEmojis.add(emoji);
    }

    String getUserId() {
        return this.id;
    }

    public List<String> getBannedEmojis() {
        return this.bannedEmojis;
    }
}
