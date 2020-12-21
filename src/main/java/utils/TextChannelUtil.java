package utils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.channel.ChannelInfo;
import commands.channel.prompt.ChannelQuestion;
import commands.quiz.QuizQuestion;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public final class TextChannelUtil {
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[^\\w\\s -]");

    private TextChannelUtil() {
    }

    public static TextChannel getChannel(String channelId, JDA jda) {
        List<TextChannel> textChannels = jda.getTextChannels();
        return textChannels.stream().filter(textChannel -> textChannel.getId().equals(channelId))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(
                String.format("Could not find channel: %s", channelId)));
    }

    public static void addNewThread(CommandEvent event, EventWaiter waiter,
        BiConsumer<CommandEvent, ChannelInfo> createChannelFunc) {

        String topic = event.getArgs();
        if (topic.isEmpty()) {
            ChannelQuestion.perform(event, waiter, createChannelFunc);
        } else {
            createNewThread(event, new ChannelInfo(topic, topic, true),
                createChannelFunc);
        }
    }

    public static void createNewThread(CommandEvent event, ChannelInfo threadInfo,
        BiConsumer<CommandEvent, ChannelInfo> createChannelFunc) {
        try {
            String name = threadInfo.getName();
            ArgumentChecker.checkIfArgsAreNotEmpty(name);
            validateName(name, event);
            String description = threadInfo.getDescription();
            checkNoBannedWords(description, event);
            createChannelFunc.accept(event, threadInfo);
        } catch (IllegalArgumentException | IllegalStateException e) {
            event.replyWarning(String.format("%s %s",
                event.getMessage().getAuthor().getAsMention(), e.getMessage()));
        }
    }

    public static void validateName(String topic, CommandEvent event) {
        if (StringUtils.isNotEmpty(topic) && topic.length() >= 2 && topic.length() <= 100) {
            topic = topic.replaceAll("'", "");
            Matcher matcher = SYMBOL_PATTERN.matcher(topic);
            if (matcher.find()) {
                throw new IllegalArgumentException(
                    String.format("Invalid name. The name \"**%s**\" can not contain special characters.",
                        topic));
            }
            checkNoBannedWords(topic, event);
        } else {
            throw new IllegalArgumentException("Name can not be empty and must be between 2-100 characters.");
        }
    }

    private static void checkNoBannedWords(String topic, CommandEvent event) {
        String badWord = WordBlacklist.searchBadWord(topic);
        if (StringUtils.isNotEmpty(badWord)) {
            User owner = event.getJDA().getUserById(event.getClient().getOwnerId());
            event.reply(owner.getAsMention() + " says: \nhttps://i.makeagif.com/media/2-21-2015/RDVwim.gif");
            MessageUtil.sendMessageToUser(owner,
                String.format("%s: %s%n%s",
                    event.getAuthor().getAsTag(), topic, event.getAuthor().getAsMention()));
            throw new IllegalArgumentException(String.format("Found blacklisted phrase **%s** in topic name", badWord));
        }
    }

    public static void blockQuizUsersForChannel(TextChannel channel, CommandEvent event) {
        Guild guild = GuildUtil.getGuild(event.getJDA());
        setDenyForRole(channel, guild, QuizQuestion.QUIZ_ROLE, Permission.MESSAGE_READ);
        setDenyForRole(channel, guild, QuizQuestion.RULES_ROLE, Permission.MESSAGE_READ);
        setDenyForRole(channel, guild, guild.getPublicRole().getName(), Permission.CREATE_INSTANT_INVITE);
    }

    private static void setDenyForRole(TextChannel channel, Guild guild, String roleName, Permission permission) {
        Role role = RoleUtil.findRole(guild, roleName);
        List<PermissionOverride> threadPermissions = channel.getRolePermissionOverrides();
        boolean roleIsNotDeniedForChannel = threadPermissions.stream()
            .noneMatch(perm -> perm.getDenied().contains(permission));

        /**TODO: This doesn't seem needed due to channel syncing**/
        if (roleIsNotDeniedForChannel) {
            channel.createPermissionOverride(role)
                .setDeny(permission)
                .queue();
        }
    }
}

