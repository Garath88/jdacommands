package commands.say;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.api.entities.User;
import utils.ArgumentChecker;
import utils.MessageUtil;
import utils.UserUtil;

public class ReadDMHistoryCommand extends Command {

    public ReadDMHistoryCommand(String name) {
        this.name = String.format("%s_dm_history", name.toLowerCase());
        this.help = String.format("reads %s DM history with a user.", name);
        this.arguments = "<user id> <number of messages>";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String args = event.getArgs();
            validateArguments(args);
            String[] items = args.split("\\s");
            User user = UserUtil.findUser(items[0], event);
            user.openPrivateChannel().queue(
                pc -> pc.getIterableHistory().limit(Integer.valueOf(items[1])).queue(
                    messages -> MessageUtil.sendMessagesToUser(event.getAuthor(), Lists.reverse(messages)),
                    fail -> {
                    }),
                fail -> {
                });
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private void validateArguments(String args) {
        ArgumentChecker.checkArgsBySpaceRequires(args, 2);
        String[] items = args.split("\\s");
        String userId = items[0];
        String limit = items[1];
        Preconditions.checkArgument(StringUtils.isNumeric(userId),
            String.format("Invalid user id \"%s\", id must be numeric", userId));
        Preconditions.checkArgument(StringUtils.isNumeric(limit),
            String.format("Invalid limit \"%s\", number must be numeric", limit));
    }
}