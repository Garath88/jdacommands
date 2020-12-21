package commands.say;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import net.dv8tion.jda.api.entities.TextChannel;
import utils.ArgumentChecker;
import utils.TextChannelUtil;

public final class SetChanCommand extends Command {
    public SetChanCommand(String name) {
        this.name = String.format("%s_set_chan", name.toLowerCase());
        this.help = String.format("Sets a channel where %s can talk in.", name);
        this.arguments = "<channel id>";
        this.requiredRoles = Permissions.MODERATOR.getValues();
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String args = event.getArgs();
            validateInput(args);
            TextChannel channel = TextChannelUtil.getChannel(args, event.getJDA());
            SayStorage.setChannel(channel.getId());
            event.reply(
                String.format("Now talking in channel: **%s** ", channel.getName()));
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private void validateInput(String args) {
        ArgumentChecker.checkArgsBySpaceRequires(args, 1);
        Preconditions.checkArgument(StringUtils.isNumeric(args),
            String.format("Invalid channel id \"%s\", id must be numeric", args));
    }
}