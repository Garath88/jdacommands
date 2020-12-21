package commands.say;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import utils.ArgumentChecker;

public class SayEditLastCommand extends Command {

    public SayEditLastCommand(String name) {
        this.name = String.format("%s_say_edit_last", name.toLowerCase());
        this.aliases = new String[] { name.substring(0, 1) + "el" };
        this.help = String.format("Edit the last message by %s", name);
        this.arguments = "<new message>";
        this.guildOnly = true;
        this.requiredRoles = Permissions.MODERATOR.getValues();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String newMessage = event.getArgs();
            ArgumentChecker.checkIfArgsAreNotEmpty(newMessage);
            String lastMessageId = SayStorage.getLastMessageId();
            if (lastMessageId == null) {
                throw new IllegalArgumentException("I don't have a last message!");
            } else {
                SayEditCommand.editMessage(lastMessageId, newMessage, event);
            }
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }
}

