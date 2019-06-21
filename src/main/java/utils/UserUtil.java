package utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;

import commands.Permissions;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

public final class UserUtil {
    private UserUtil() {
    }

    public static User findUser(String userId, CommandEvent event) {
        JDA jda = event.getJDA();
        userId = userId.replaceAll("\\s+", "");
        User user = FinderUtil.findUsers(userId, jda).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No user with that ID found!"));
        User self = FinderUtil.findUsers(
            event.getAuthor().getId().replaceAll("\\s+", ""), jda)
            .stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
        if (user.getId().equals(self.getId())) {
            throw new IllegalArgumentException("The ID can't be yourself!");
        }
        return user;
    }

    public static boolean isNotModAdminOrBot(String userId, JDA jda) {
        userId = userId.replaceAll("\\s+", "");
        User user = FinderUtil.findUsers(userId, jda).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No user with that ID found!"));
        if (user.isBot()) {
            return false;
        }
        return GuildUtil.getGuild(jda).getMember(user).getRoles().stream()
            .noneMatch(role -> Permissions.MODERATOR.getValues().contains(role.getName()));
    }
}