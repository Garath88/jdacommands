package commands.misc;

import java.util.ArrayList;
import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;
import utils.ArgumentChecker;

public class ChannelSpacingCommand extends Command {
    public ChannelSpacingCommand() {
        this.name = "channels";
        this.help = "if you wish to have spaces in your channel names.";
        this.arguments = "<space|dash>";
        this.requiredRoles = Permissions.MODERATOR.getValues();
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String arguments = event.getArgs();
            ArgumentChecker.checkArgsBySpaceRequires(arguments, 1);
            List<TextChannel> textChannels = event.getGuild()
                .getTextChannels();
            if (arguments.equals("dash")) {
                String dashedChannels = spaceOrDashChannels(event, textChannels, " ", "-");
                if (!dashedChannels.isEmpty()) {
                    event.reply(String.format("Dashed channels: %s", dashedChannels));
                }
            } else if (arguments.equals("space")) {
                String spacedChannels = spaceOrDashChannels(event, textChannels, "-", "` `");
                if (!spacedChannels.isEmpty()) {
                    event.reply(String.format("Spaced channels: %s", spacedChannels));
                }
            }
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private String spaceOrDashChannels(CommandEvent event, List<TextChannel> channels, String from, String to) {
        List<String> successChannelNames = new ArrayList<>();
        List<String> failChannelNames = new ArrayList<>();
        channels.forEach(channel -> {
            if (channel.getName().contains(from)) {
                if (!PermissionUtil.checkPermission(channel, event.getSelfMember(),
                    Permission.MANAGE_CHANNEL)) {
                    failChannelNames.add(String.format("%s", channel.getAsMention()));
                } else {
                    successChannelNames.add(String.format("**%s**", channel.getName()));
                    String newName = String.join(to, channel.getName().split(from));
                    channel.getManager().setName(newName)
                        .queue();
                }
            }
        });
        if (!failChannelNames.isEmpty()) {
            String failed = String.join(",", failChannelNames);
            event.reply(String.format("Lacking permission to rename channel: %s", failed));
        }
        return String.join(", ", successChannelNames);
    }
}