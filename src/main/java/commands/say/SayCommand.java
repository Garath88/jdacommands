package commands.say;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import commands.thread.ThreadCommand;
import commands.thread.ThreadInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
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
        this.aliases = new String[] { name.substring(0, 1) + "s" };
        this.help = String.format("say something with %s and optionally create a channel"
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
                    ThreadCommand.createNewThread(event,
                        new ThreadInfo(name, name, false));
                }
            }
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private void say(CommandEvent event, String message) {
        String channelId = SayStorage.getChannelId().orElseThrow(() -> new IllegalArgumentException(String.format("You haven't added a text channel to talk in! \n "
            + "Please use the **%s%s_set_chan** command", event.getClient().getPrefix(), botName.toLowerCase())));
        TextChannel textChannel = TextChannelUtil.getChannel(channelId, event.getEvent());
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (StringUtils.isNotEmpty(message) || !attachments.isEmpty()) {
            if (event.isFromType(ChannelType.PRIVATE)) {
                message = MessageUtil.addMentionsAndEmojis(message, event.getJDA());
            }
            MessageUtil.sendAttachmentsToChannel(attachments, textChannel);
            MessageUtil.sendSayCommandMessageToChannel(message, textChannel, SayStorage.getUseDash());
        } else {
            event.reply(String.format("Currently talking in channel: **%s**",
                textChannel.getName()));
        }
    }
}
