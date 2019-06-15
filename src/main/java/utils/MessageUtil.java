package utils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public final class MessageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtil.class);

    private MessageUtil() {
    }

    public static void sendMessageToChannel(String message, MessageChannel channel, boolean usePrefix) {
        if (!StringUtils.isEmpty(message)) {
            if (usePrefix) {
                message = "- " + message;
            }
            MessageUtil.sendMessageToChannel(channel, message);
        }
    }

    public static void sendMessageToUser(User user, Message message) {
        user.openPrivateChannel()
            .queue(PrivateChannelWrapper.userIsInGuild(pc ->
                MessageUtil.sendMessageToChannel(pc, message)));
    }

    public static void sendMessageToUser(User user, String message) {
        user.openPrivateChannel()
            .queue(PrivateChannelWrapper.userIsInGuild(pc ->
                MessageUtil.sendMessageToChannel(pc, message)));
    }

    private static void sendMessageToChannel(MessageChannel channel, Message message) {
        sendMessageToChannel(channel, message.getContentRaw());
        sendEmbedsToChannel(channel, message.getEmbeds());
        List<Message.Attachment> attachments = message.getAttachments();
        if (!attachments.isEmpty()) {
            sendAttachmentsToChannel(attachments, channel);
        }
    }

    private static void sendEmbedsToChannel(MessageChannel channel, List<MessageEmbed> embeds) {
        embeds.forEach(embed -> channel.sendMessage(embed).queue());
    }

    public static void sendAttachmentsToChannel(List<Attachment> attachments, MessageChannel channel) {
        attachments.forEach(attachment -> {
            try {
                channel.sendFile(attachment.getInputStream(), attachment.getFileName())
                    .queue();
            } catch (IOException e) {
                LOGGER.error("Failed to add attachment", e);
            }
        });
    }

    private static void sendMessageToChannel(MessageChannel channel, String message) {
        if (!message.isEmpty()) {
            channel.sendMessage(message)
                .queue(success -> {
                }, fail -> {
                });
        }
    }

    public static void waitForResponseInDM(User user, Guild guild, EventWaiter waiter,
        TriFunction<Guild, MessageReceivedEvent, EventWaiter> dmResponse,
        int timeoutMinutes, String retryMsg) {

        checkResponseInDM(e -> dmResponse.apply(guild, e, waiter),
            waiter, user, timeoutMinutes, retryMsg);
    }

    private static void checkResponseInDM(Consumer<MessageReceivedEvent> dmResponse,
        EventWaiter waiter, User user, int timeoutMinutes,
        String retryMsg) {
        waiter.waitForEvent(MessageReceivedEvent.class,
            e -> e.getAuthor().equals(user) && e.getChannel().getType().equals(ChannelType.PRIVATE),
            dmResponse, timeoutMinutes, TimeUnit.MINUTES, () -> MessageUtil.sendMessageToUser(user,
                String.format("- Sorry you were too slow to respond %s :frowning: \n"
                    + retryMsg, user.getAsMention())), user);
    }

    public static void waitForResponseInChannel(CommandEvent event, EventWaiter waiter,
        Consumer<MessageReceivedEvent> channelResponse,
        int timeoutMinutes, String retryMsg) {
        User user = event.getAuthor();

        checkResponseInChannel(channelResponse,
            waiter, user, timeoutMinutes, retryMsg, event);
    }

    private static void checkResponseInChannel(Consumer<MessageReceivedEvent> channelResponse,
        EventWaiter waiter, User user, int timeoutMinutes,
        String retryMsg, CommandEvent event) {
        waiter.waitForEvent(MessageReceivedEvent.class,
            e -> e.getAuthor().equals(user) && e.getChannel().getId().equals(event.getChannel().getId()),
            channelResponse, timeoutMinutes, TimeUnit.MINUTES, () -> event.reply(
                String.format("- Sorry you were too slow to respond %s :frowning: \n"
                    + retryMsg, user.getAsMention())), user);
    }
}
