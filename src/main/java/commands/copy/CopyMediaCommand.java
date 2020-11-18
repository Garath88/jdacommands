package commands.copy;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import utils.ArgumentChecker;
import utils.MessageUtil;
import utils.TextChannelUtil;

public class CopyMediaCommand extends Command {
    private static final int MAX_HISTORY_LIMIT = 10;
    private TextChannel fromChannel;
    private TextChannel toChannel;
    private static final int MINIMUM_ARGS = 3;

    public CopyMediaCommand() {
        this.name = "copy_media";
        this.help = "copies the attachments from one channel to another.";
        this.arguments = "<from chan id> <to chan id> <starting msg id> [end msg id]";
        this.ownerCommand = true;
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL,
            Permission.MESSAGE_READ
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String[] items = event.getArgs().split("\\s");
            validateInput(items);
            String lastId = items.length > MINIMUM_ARGS ? items[3] : "";
            JDA jda = event.getJDA();
            fromChannel = TextChannelUtil.getChannel(items[0], jda);
            toChannel = TextChannelUtil.getChannel(items[1], jda);
            fromChannel.retrieveMessageById(items[2]).queue(
                firstMsg -> {
                    checkMessageForPatternMatch(firstMsg.getContentRaw(), toChannel);
                    MessageUtil.sendAttachmentsToChannel(firstMsg.getAttachments(), toChannel);
                    postImages(items[2], MAX_HISTORY_LIMIT, lastId);
                }, fail -> event.replyWarning("Could not find first message"));
        } catch (IllegalArgumentException | InsufficientPermissionException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private void validateInput(String[] args) {
        ArgumentChecker.checkArgsBySpaceIsAtLeast(Arrays.toString(args), MINIMUM_ARGS);
        for (String item : args) {
            Preconditions.checkArgument(StringUtils.isNumeric(item),
                String.format("Invalid id \"%s\", id must be numeric", item));
        }
    }

    private void postImages(String msgId, int maxLimit, String endId) {
        fromChannel.getHistoryAfter(msgId, maxLimit).queue(
            history -> {
                List<Message> messages = history.getRetrievedHistory();
                if (!messages.isEmpty()) {
                    Iterator<Message> it = Lists.reverse(messages).iterator();
                    while (it.hasNext()) {
                        Message message = it.next();
                        sendMedia(message, toChannel);
                        if (message.getId().equals(endId)) {
                            break;
                        } else if (!it.hasNext()) {
                            postImages(message.getId(), maxLimit, endId);
                        }
                    }
                }
            }
        );
    }

    public static void sendMedia(Message message, TextChannel toChannel) {
        checkMessageForPatternMatch(message.getContentRaw(), toChannel);
        MessageUtil.sendAttachmentsToChannel(message.getAttachments(), toChannel);
    }

    private static void checkMessageForPatternMatch(String contentRaw, TextChannel toChannel) {
        MediaPatterns.getMediaPatterns().forEach(pattern ->
            sendMessageIfMatch(pattern.matcher(contentRaw), toChannel));
    }

    private static void sendMessageIfMatch(Matcher matcher, TextChannel toChannel) {
        if (matcher.find()) {
            toChannel.sendMessage(matcher.group(0))
                .queue();
        }
    }
}