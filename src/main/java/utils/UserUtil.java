package utils;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;

import commands.Permissions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

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
        final String id = userId.replaceAll("\\s+", "");
        User user = FinderUtil.findUsers(id, jda).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("No user with ID %s found!",
                userId)));
        if (user.isBot()) {
            return false;
        }
        return GuildUtil.getGuild(jda).getMember(user).getRoles().stream()
            .noneMatch(role -> Permissions.MODERATOR.getValues().contains(role.getName()));
    }

    public static boolean isNotModAdminOrBot(User user, JDA jda) {
        if (user.isBot()) {
            return false;
        }
        return GuildUtil.getGuild(jda).getMember(user).getRoles().stream()
            .noneMatch(role -> Permissions.MODERATOR.getValues().contains(role.getName()));
    }

    public static String validateAndGetUser(String user, CommandEvent event) {
        if ("all".equalsIgnoreCase(user.toLowerCase())) {
            return "all";
        } else {
            Preconditions.checkArgument(StringUtils.isNumeric(user),
                String.format("Invalid user id \"%s\", id must be numeric", user));
            return UserUtil.findUser(user, event).getAsMention();
        }
    }
}