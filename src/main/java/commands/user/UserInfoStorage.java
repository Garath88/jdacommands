package commands.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.core.JDA;
import utils.EmojiUtil;

public final class UserInfoStorage {
    private static List<UserInfo> users = new ArrayList<>();

    private UserInfoStorage() {
    }

    public static void addEmojiAndUser(String emoji, String userId) {
        Optional<UserInfo> userInfo = findUser(userId);
        if (userInfo.isPresent()) {
            UserInfo user = userInfo.get();
            if (user.getBannedEmojis().contains(emoji)) {
                throw new IllegalArgumentException("Emoji already banned!");
            }
            userInfo.get().addEmoji(emoji);
        } else {
            UserInfo newUserInfo = new UserInfo(userId);
            newUserInfo.addEmoji(emoji);
            users.add(newUserInfo);
        }
    }

    public static Optional<UserInfo> findUser(String id) {
        return users.stream()
            .filter(userInfo -> userInfo.getUserId().equals(id))
            .findAny();
    }

    public static String listBannedEmojisForAllUsers(JDA jda) {
        String ret;
        StringBuilder sb = new StringBuilder();
        if (users.isEmpty()) {
            sb.append("No banned emojis!");
            ret = sb.toString();
        } else {
            sb.append("Listing banned emojis:\n");
            users.forEach(user -> {
                String bannedEmojis = String.join(" ", user.getBannedEmojis());
                bannedEmojis = bannedEmojis.replaceAll("[<>0-9]", "");
                String userId = user.getUserId();
                if (userId.equals("all")) {
                    sb.append(String.format("All: %s %n",
                        bannedEmojis));
                } else {
                    sb.append(String.format("<@%s> %s %n",
                        userId, bannedEmojis));
                }
            });
            ret = EmojiUtil.addEmojisToMessage(jda, sb.toString());
        }
        return ret;
    }

    public static void removeEmojiForUser(String emoji, String userId) {
        Optional<UserInfo> user = findUser(userId);
        user.ifPresent(info -> {
            info.removeEmoji(emoji);
            if (info.getBannedEmojis().isEmpty()) {
                users.remove(info);
            }
        });
    }
}
