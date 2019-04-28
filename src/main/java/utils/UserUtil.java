package utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;

import net.dv8tion.jda.core.entities.User;

public final class UserUtil {
    private UserUtil() {
    }

    public static User findUser(String userId, CommandEvent event) {
        userId = userId.replaceAll("\\s+", "");
        User user = FinderUtil.findUsers(userId, event.getJDA()).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No user with that ID found!"));
        User self = FinderUtil.findUsers(
            event.getAuthor().getId().replaceAll("\\s+", ""), event.getJDA())
            .stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
        if (user.getId().equals(self.getId())) {
            throw new IllegalArgumentException("The ID can't be yourself!");
        }
        return user;
    }
}