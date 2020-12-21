package commands.emoji;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import commands.user.UserInfoStorage;
import net.dv8tion.jda.api.Permission;
import utils.ArgumentChecker;
import utils.EmojiUtil;
import utils.UserUtil;

public final class RemoveEmojiBanCommand extends Command {

    public RemoveEmojiBanCommand() {
        this.name = "ban_emoji_rm";
        this.help = "Removes an emoji ban for a user or all members.";
        this.arguments = "<emoji> <user id> or <all>";
        this.guildOnly = true;
        this.requiredRoles = Permissions.MODERATOR.getValues();
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String args = event.getArgs();
            ArgumentChecker.checkArgsBySpaceRequires(args, 2);
            String[] items = args.split("\\s");
            validateEmoji(items[0]);
            String user = UserUtil.validateAndGetUser(items[1], event);
            UserInfoStorage.removeEmojiForUser(items[0], items[1]);
            event.reply(String.format("Successfully removed emoji %s from user %s",
                items[0], user));
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private void validateEmoji(String emoji) {
        if (EmojiUtil.isNotEmoji(emoji)) {
            throw new IllegalArgumentException(String.format("Invalid emoji syntax %s", emoji));
        }
    }
}
