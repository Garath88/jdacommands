package utils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.say.SayStorage;
import net.dv8tion.jda.core.JDA;
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

    public static void sendSayCommandMessageToChannel(String message, MessageChannel channel, boolean usePrefix) {
        if (!StringUtils.isEmpty(message)) {
            if (usePrefix) {
                message = "- " + message;
            }
            if (!message.isEmpty()) {
                channel.sendMessage(message)
                    .queue(success ->
                            SayStorage.setLastMessageId(success.getId()),
                        fail -> {
                        });
            }
        }
    }

    public static void sendMessageToChannel(String message, MessageChannel channel, boolean usePrefix) {
        if (!StringUtils.isEmpty(message)) {
            if (usePrefix) {
                message = "- " + message;
            }
            MessageUtil.sendMessageToChannel(message, channel);
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
                MessageUtil.sendMessageToChannel(message, pc)));
    }

    private static void sendMessageToChannel(MessageChannel channel, Message message) {
        sendMessageToChannel(message.getContentRaw(), channel);
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

    private static void sendMessageToChannel(String message, MessageChannel channel) {
        if (!message.isEmpty()) {
            channel.sendMessage(message)
                .queue(success -> {
                }, fail -> {
                });
        }
    }

    static void sendMessageToChannelAndDelete(String message, MessageChannel channel, int delayInMillis) {
        if (!message.isEmpty()) {
            channel.sendMessage(message)
                .queue(sentMessage ->
                        TimeoutUtil.setTimeout(() -> sentMessage.delete().queue(), delayInMillis),
                    fail -> {
                    });
        }
    }

    public static void waitForResponseInDM(User user, Guild guild, EventWaiter waiter,
        TriFunction<Guild, MessageReceivedEvent, EventWaiter> dmResponse,
        int timeoutSeconds, String retryMsg, CommandClient client) {

        checkResponseInDM(e -> dmResponse.apply(guild, e, waiter),
            waiter, user, timeoutSeconds, retryMsg, client);
    }

    private static void checkResponseInDM(Consumer<MessageReceivedEvent> dmResponse,
        EventWaiter waiter, User user, int timeoutSeconds,
        String retryMsg, CommandClient client) {
        waiter.waitForEvent(MessageReceivedEvent.class,
            e -> e.getAuthor().equals(user) && e.getChannel().getType().equals(ChannelType.PRIVATE)
                && doesNotContainCommand(e, client.getCommandArguments()),
            dmResponse, timeoutSeconds, TimeUnit.SECONDS, () -> MessageUtil.sendMessageToUser(user,
                String.format("- Sorry you were too slow to respond %s :frowning: \n"
                    + retryMsg, user.getAsMention())), user);
    }

    public static void waitForResponseInChannel(CommandEvent event, EventWaiter waiter,
        Consumer<MessageReceivedEvent> channelResponse,
        int timeoutSeconds, String retryMsg) {
        User user = event.getAuthor();

        checkResponseInChannel(channelResponse,
            waiter, user, timeoutSeconds, retryMsg, event);
    }

    private static void checkResponseInChannel(Consumer<MessageReceivedEvent> channelResponse,
        EventWaiter waiter, User user, int timeoutSeconds,
        String retryMsg, CommandEvent event) {
        waiter.waitForEvent(MessageReceivedEvent.class,
            e -> e.getAuthor().equals(user) && e.getChannel().getId().equals(event.getChannel().getId())
                && doesNotContainCommand(e, event.getClient().getCommandArguments()),
            channelResponse, timeoutSeconds, TimeUnit.SECONDS, () -> event.reply(
                String.format("- Sorry you were too slow to respond %s :frowning: \n"
                    + retryMsg, user.getAsMention())), user);
    }

    private static boolean doesNotContainCommand(MessageReceivedEvent event, List<String> commandArguments) {
        String[] message = event.getMessage().getContentRaw().toLowerCase().split("\\s");
        return !commandArguments.contains(message[0]);
    }

    public static String addMentionsAndEmojis(String message, JDA jda) {
        message = MentionUtil.addMentionsToMessage(message, jda);
        message = EmojiUtil.addEmojisToMessage(message, jda);
        return message;
    }
}
