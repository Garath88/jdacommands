package commands.emoji;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import commands.user.UserInfo;
import commands.user.UserInfoStorage;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import utils.ArgumentChecker;
import utils.EmojiUtil;
import utils.MessageCycler;
import utils.UserUtil;

public final class BanEmojiCommand extends Command {
    private static final int DELETE_TIMEOUT = 180_000;
    private static final MessageCycler messageCycler = new MessageCycler(Arrays.asList(
        "- You miscreants are getting on my nerves...time to take away your loud toys!",
        "- Nonono, be a good worm and behave.",
        "- How about... NO!",
        "- Another disgusting display wiped away."
    ), 10000, false);

    public BanEmojiCommand() {
        this.name = "ban_emoji";
        this.help = "automatically delete reactions and messages with the emoji for a user or all members.\nList deleted emojis with no arguments";
        this.arguments = "<emoji> <user id> or <all>";
        this.guildOnly = true;
        this.requiredRoles = Permissions.MODERATOR.getValues();
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL,
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_WRITE,
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String args = event.getArgs();
            if (args.isEmpty()) {
                event.reply(UserInfoStorage.listBannedEmojisForAllUsers(event.getJDA()));
            } else {
                ArgumentChecker.checkArgsBySpaceRequires(args, 2);
                String[] items = args.split("\\s");
                validateEmoji(items[0]);
                String user = UserUtil.validateAndGetUser(items[1], event);
                UserInfoStorage.addEmojiAndUser(items[0], items[1]);
                event.reply(String.format("Successfully added emoji %s to blacklist for user %s",
                    items[0], user));
                messageCycler.resetMessageCounter();
            }
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private void validateEmoji(String emoji) {
        if (EmojiUtil.isNotEmoji(emoji)) {
            throw new IllegalArgumentException(String.format("Invalid emoji syntax %s", emoji));
        }
    }

    public static void deleteMessageWithBlacklistedEmojis(String userId, Message message) {
        if (UserUtil.isNotModAdminOrBot(userId, message.getJDA())) {
            Optional<UserInfo> userInfo = UserInfoStorage.findUser(userId);
            userInfo.ifPresent(info -> removeEmojiFromMessage(message, info.getBannedEmojis()));
            Optional<UserInfo> allUsers = UserInfoStorage.findUser("all");
            allUsers.ifPresent(info -> removeEmojiFromMessage(message, info.getBannedEmojis()));
        }
    }

    public static void deleteReactionWithBlacklistedEmojis(User author, MessageReaction messageReaction) {
        if (UserUtil.isNotModAdminOrBot(author, messageReaction.getJDA())) {
            removeBlacklistedEmojiForId(author, messageReaction);
        }
    }

    private static void removeBlacklistedEmojiForId(User author, MessageReaction messageReaction) {
        Optional<UserInfo> userInfo = UserInfoStorage.findUser(author.getId());
        userInfo.ifPresent(info ->
            removeEmojiFromReaction(author, messageReaction, info.getBannedEmojis()));
        userInfo = UserInfoStorage.findUser("all");
        userInfo.ifPresent(info ->
            removeEmojiFromReaction(author, messageReaction, info.getBannedEmojis()));
    }

    private static void removeEmojiFromMessage(Message message, List<String> bannedEmojis) {
        if (bannedEmojis.stream()
            .anyMatch(emoji -> message.getContentRaw().contains(emoji))) {
            message.delete().queue();
            messageCycler.replyWithMessageAndDeleteAfterDelay(message.getChannel(), DELETE_TIMEOUT);
        }
    }

    private static void removeEmojiFromReaction(User author, MessageReaction messageReaction, List<String> bannedEmojis) {
        if (bannedEmojis.stream()
            .anyMatch(emoji -> emoji.contains(messageReaction.getReactionEmote().getName()))) {
            messageReaction.removeReaction(author).queue();
            messageCycler.replyWithMessageAndDeleteAfterDelay(messageReaction.getChannel(), DELETE_TIMEOUT);
        }
    }
}
