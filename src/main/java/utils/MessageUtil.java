package utils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.say.SayStorage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Guild.BoostTier;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.requests.Route.Guilds;

public final class MessageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtil.class);
    private static final int MAX_UPLOAD_8_MB = 8_000_000;

    private MessageUtil() {
    }

    public static void sendMessagesToUser(User user, List<Message> messages) {
        user.openPrivateChannel()
            .queue(PrivateChannelWrapper.userIsInGuild(pc ->
                MessageUtil.sendMessagesToChannel(pc, messages, 0)));
    }

    public static void sendMessageToUser(User user, String message, int delayInMillis) {
        user.openPrivateChannel()
            .queue(PrivateChannelWrapper.userIsInGuild(pc -> {
                pc.sendTyping().queue();
                MessageUtil.sendMessageToChannel(message, pc, delayInMillis);
            }));
    }

    /**TODO Refactor this..*/
    private static void sendMessagesToChannel(MessageChannel channel, List<Message> messages, int messageCounter) {
        if (messageCounter < messages.size()) {
            Message message = messages.get(messageCounter);
            messageCounter++;
            final int counter = messageCounter;
            List<Attachment> attachments = message.getAttachments();
            if (!message.getContentRaw().isEmpty()) {
                MessageAction action = channel.sendMessage(message);
                if (attachments.size() == 1) {
                    Attachment file = attachments.get(0);
                    if (file.getSize() <= MAX_UPLOAD_8_MB) {
                        file.retrieveInputStream().thenAccept(data -> action.addFile(data, file.getFileName()).queue(
                            success -> sendMessagesToChannel(channel, messages, counter)));
                    } else {
                        action.queue(success -> channel.sendMessage(file.getUrl()).queue(
                            success2 -> sendMessagesToChannel(channel, messages, counter)));
                    }
                } else {
                    action.queue(success -> sendMessagesToChannel(channel, messages, counter));
                }
            } else {
                attachments.forEach(attachment -> {
                    if (attachment.getSize() <= MAX_UPLOAD_8_MB) {
                        attachment.retrieveInputStream()
                            .thenAccept(data -> channel.sendFile(data, attachment.getFileName())
                                .queue(success -> sendMessagesToChannel(channel, messages, counter)))
                            .exceptionally(e -> {
                                LOGGER.error("ATTACHMENT_ERROR_MSG", e);
                                return null;
                            });
                    } else {
                        channel.sendMessage(attachment.getUrl()).queue(
                            success2 -> sendMessagesToChannel(channel, messages, counter));
                    }
                });
            }
        }
    }

    public static void sendAttachmentsAndSayTextToChannel(List<Attachment> attachments, String message, MessageChannel channel) {
        if (!attachments.isEmpty()) {
            sendAttachmentsToChannel(attachments, channel);
            sendSayTextMessageToChannel(message, channel);
        } else {
            sendSayTextMessageToChannel(message, channel);
        }
    }

    private static void sendSayTextMessageToChannel(String message, MessageChannel channel) {
        if (!StringUtils.isEmpty(message)) {
            if (SayStorage.getUseDash()) {
                message = "- " + message;
            }
            final String sayMessage = message;
            if (!sayMessage.isEmpty()) {
                channel.sendMessage(sayMessage)
                    .queue(success ->
                            SayStorage.setLastMessageId(success.getId()),
                        fail -> LOGGER.error(String.format(
                            "Failed to setLastMessageId for message: %s",
                            sayMessage), fail));
            }
        }
    }

    public static void sendAttachmentsToChannel(List<Message.Attachment> attachments, MessageChannel channel) {
        attachments.forEach(attachment -> {
            try {
                if (attachment.getSize() <= MAX_UPLOAD_8_MB ||
                    GuildUtil.getGuild(channel.getJDA()).getBoostTier().equals(BoostTier.TIER_2)) {
                    channel.sendFile(attachment.retrieveInputStream().get(), attachment.getFileName())
                        .queue();
                } else {
                    channel.sendMessage(attachment.getUrl())
                        .queue();
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Thread got interrupted while trying to send attachment", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    private static void sendMessageToChannel(String message, MessageChannel channel) {
        if (!message.isEmpty()) {
            channel.sendMessage(message)
                .queue(success -> {
                }, fail -> LOGGER.error("Failed to sendMessageToChannel for message: " + message, fail));
        }
    }

    public static void sendMessageToUser(User user, String message) {
        user.openPrivateChannel()
            .queue(PrivateChannelWrapper.userIsInGuild(pc ->
                MessageUtil.sendMessageToChannel(message, pc)));
    }

    private static void sendMessageToChannel(String message, MessageChannel channel, int delayInMillis) {
        if (!message.isEmpty()) {
            channel.sendMessage(message)
                .queueAfter(delayInMillis, TimeUnit.MILLISECONDS, success -> {
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
