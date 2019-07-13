package commands.say;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import utils.ArgumentChecker;
import utils.PrivateChannelWrapper;
import utils.UserUtil;

public class DMDeleteCommand extends Command {

    public DMDeleteCommand(String name) {
        this.name = String.format("%s_dm_delete", name.toLowerCase());
        this.help = "deletes the last DM sent";
        this.arguments = "<user id>";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String args = event.getArgs();
            validateArguments(args);
            User user = UserUtil.findUser(args, event);
            user.openPrivateChannel().queue(
                PrivateChannelWrapper.userIsInGuild(pc -> pc.getIterableHistory().limit(30)
                    .queue(DMDeleteCommand::deleteLatestMessage)),
                fail -> {
                });
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private void validateArguments(String args) {
        ArgumentChecker.checkArgsByPipe(args, 1);
        String userId = args.replaceAll("\\s+", "");
        Preconditions.checkArgument(StringUtils.isNumeric(userId),
            String.format("Invalid user id \"%s\", id must be numeric", userId));
    }

    private static void deleteLatestMessage(List<Message> history) {
        history.stream()
            .filter(msg -> msg.getAuthor().isBot())
            .findFirst()
            .ifPresent(latestMsg -> latestMsg.delete()
                .queue());
    }
}

