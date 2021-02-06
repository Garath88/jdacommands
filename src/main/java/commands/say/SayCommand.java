package commands.say;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import commands.channel.thread.ThreadCommand;
import commands.channel.ChannelInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import utils.MessageUtil;
import utils.TextChannelUtil;

public final class SayCommand extends Command {
    private static final int MESSAGE_INDEX = 0;
    private static final int THREAD_INDEX = 1;
    private static final int MESSAGE_WITH_CHANNEL_ID = 2;
    private final String botName;

    public SayCommand(String name) {
        botName = name;
        this.name = String.format("%s_say", name.toLowerCase());
        this.aliases = new String[] { this.name.substring(0, 1) + "s" };
        this.help = String.format("Say something with %s and optionally create a channel"
            + " or with no arguments to list current talking channel.", name);
        this.arguments = "[<text>] followed by separator '|' [<topic>]";
        this.guildOnly = true;
        this.requiredRoles = Permissions.MODERATOR.getValues();
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String[] message = event.getArgs().split("\\|");
            if (message.length == 1 && message[0].equals("-")) {
                SayStorage.toggleUseDash(event);
            } else {
                say(event, message[MESSAGE_INDEX]);
                if (message.length == MESSAGE_WITH_CHANNEL_ID) {
                    String name = message[THREAD_INDEX].trim();
                    TextChannelUtil.createNewThread(event,
                        new ChannelInfo(name, name, false),
                        ThreadCommand::createThreadChannel);
                }
            }
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private void say(CommandEvent event, String message) {
        String channelId = SayStorage.getChannelId().orElseThrow(() ->
            new IllegalArgumentException(String.format("You haven't added a text channel to talk in! \n "
                    + "Please use the **%s%s_set_chan** command",
                event.getClient().getPrefix(), botName.toLowerCase())));
        TextChannel textChannel = TextChannelUtil.getChannel(channelId, event.getJDA());
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (StringUtils.isNotEmpty(message) || !attachments.isEmpty()) {
            MessageUtil.sendAttachmentsAndSayTextToChannel(attachments, message, textChannel);
        } else {
            event.reply(String.format("Currently talking in channel: **%s**",
                textChannel.getName()));
        }
    }
}
