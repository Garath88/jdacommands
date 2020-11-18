package commands.say;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import net.dv8tion.jda.api.JDA;
import utils.ArgumentChecker;

public class SayEditCommand extends Command {

    public SayEditCommand(String name) {
        this.name = String.format("%s_say_edit", name.toLowerCase());
        this.aliases = new String[] { name.substring(0, 1) + "e" };
        this.help = String.format("edit a message by %s", name);
        this.arguments = "<message id> | <new message>";
        this.guildOnly = true;
        this.requiredRoles = Permissions.MODERATOR.getValues();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String args = event.getArgs();
            ArgumentChecker.checkArgsByPipe(args, 2);
            String[] items = args.split("\\|");
            String messageId = items[0].replaceAll("\\s+", "");
            editMessage(messageId, items[1], event);
        } catch (IllegalArgumentException | IllegalStateException e) {
            event.replyWarning(e.getMessage());
        }
    }

    static void editMessage(String messageId, String text, CommandEvent event) {
        Preconditions.checkArgument(StringUtils.isNumeric(messageId),
            String.format("Invalid message id \"%s\", id must be numeric", messageId));
        if (!text.isEmpty()) {
            JDA jda = event.getJDA();
            String channelId = SayStorage.getChannelId().orElseThrow(() -> new IllegalStateException("Not talking in any channel!"));
            jda.getTextChannelById(channelId).retrieveMessageById(messageId).queue(message -> {
                if (message.getAuthor().equals(event.getSelfUser())) {
                    message.editMessage(text).queue();
                } else {
                    event.replyWarning("I did not write that message!");
                }
            }, fail -> event.replyWarning("Could not find message in current set channel!"));
        }
    }
}
