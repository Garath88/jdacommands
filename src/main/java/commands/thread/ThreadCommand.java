package commands.thread;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.Permissions;
import commands.quiz.QuizQuestion;
import commands.thread.database.ThreadDbInfo;
import commands.thread.database.ThreadDbTable;
import commands.thread.prompt.ThreadQuestion;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import utils.ArgumentChecker;
import utils.CategoryUtil;
import utils.GuildUtil;
import utils.MessageUtil;
import utils.RoleUtil;
import utils.TextChannelUtil;
import utils.WordBlacklist;

public class ThreadCommand extends Command {
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[^\\w\\s -]");
    private static final int MAX_AMOUNT_OF_THREADS = 15;
    private static final int LURKER_MAX_THREAD_LIMIT = 1;
    private final EventWaiter waiter;

    public ThreadCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "thread";
        this.help = "creates a new thread with description.";
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (CategoryUtil.getThreadCategory(event.getJDA())
            .getTextChannels().size() >= MAX_AMOUNT_OF_THREADS) {
            sendErrorMsg("Sorry maximum amount of threads reached! Try again later.", event);
        } else if (isAtMaxThreadsForUser(event)) {
            sendErrorMsg(String.format(
                "Sorry you can only make %s thread(s) for your current role.",
                LURKER_MAX_THREAD_LIMIT), event);
        } else {
            addNewThread(event);
        }
    }

    private void sendErrorMsg(String msg, CommandEvent event) {
        event.reply(msg);
        event.reactError();
    }

    private boolean isAtMaxThreadsForUser(CommandEvent event) {
        if (RoleUtil.getMemberRoles(event).stream()
            .map(Role::getName)
            .noneMatch(Permissions.FAN.getValues()::contains)) {
            ThreadDbInfo threadInfo = ThreadDbTable.getThreadInfoFromUser(event.getAuthor());
            return threadInfo.getThreadIds().size() >= LURKER_MAX_THREAD_LIMIT;
        }
        return false;
    }

    private void addNewThread(CommandEvent event) {
        String topic = event.getArgs();
        if (topic.isEmpty()) {
            ThreadQuestion.perform(event, waiter);
        } else {
            createNewThread(event,
                new ThreadInfo(topic, topic, true));
        }
    }

    public static void createNewThread(CommandEvent event, ThreadInfo threadInfo) {
        try {
            String name = threadInfo.getName();
            ArgumentChecker.checkIfArgsAreNotEmpty(name);
            validateName(name, event);
            String description = threadInfo.getDescription();
            checkNoBannedWords(description, event);
            createThreadChannel(event, threadInfo);
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

    private static void createThreadChannel(CommandEvent event, ThreadInfo threadInfo) {
        net.dv8tion.jda.core.entities.Category threadCategory = CategoryUtil.getThreadCategory(event.getJDA());
        String name = threadInfo.getName();
        String description = threadInfo.getDescription();
        validateThreadName(threadCategory, name);
        final String channelTopic = name.replaceAll(" ", "` `");
        GuildUtil.getGuild(event.getJDA()).getController().createTextChannel(channelTopic)
            .setTopic(description)
            .setNSFW(true)
            .setParent(threadCategory)
            .queue(chan -> doTasks(chan, event, description, threadInfo.getStoreInDatabase()));
    }

    private static void validateThreadName(net.dv8tion.jda.core.entities.Category customCategory, String topic) {
        if (customCategory.getTextChannels().stream().anyMatch(chan -> chan.getName().equals(topic))) {
            throw new IllegalArgumentException(String.format(
                "This thread **%s** already exists! Please retry the command again.", topic));
        }
    }

    private static void doTasks(Channel threadChannel, CommandEvent event, String topic, boolean storeInDatabase) {
        Guild guild = GuildUtil.getGuild(event.getJDA());
        setDenyForRole(threadChannel, guild, QuizQuestion.QUIZ_ROLE, Permission.MESSAGE_READ);
        setDenyForRole(threadChannel, guild, QuizQuestion.RULES_ROLE, Permission.MESSAGE_READ);
        setDenyForRole(threadChannel, guild, guild.getPublicRole().getName(), Permission.CREATE_INSTANT_INVITE);

        TextChannel threadTextChannel = findThreadTextChannel(threadChannel, event.getJDA());
        if (storeInDatabase) {
            ThreadDbTable.addThread(event.getAuthor(), threadChannel);
            sendTopicHasBeenSetMsg(threadTextChannel, topic, event);
            InactiveThreadChecker.startOrCancelInactivityTaskIfNotTopX(threadTextChannel);
        } else { //Sakura thread
            ThreadDbTable.addThread(event.getSelfUser(), threadChannel);
            ThreadDbTable.storePostCount(9999,
                threadChannel.getIdLong());
        }
        event.reply(String.format("Successfully created new thread: **%s**", threadTextChannel.getAsMention()));
    }

    /*TODO Refactor this*/
    private static void setDenyForRole(Channel threadChannel, Guild guild, String roleName, Permission permission) {
        Role role = RoleUtil.findRole(guild, roleName);
        List<Permission> threadPermissions =
            threadChannel.getPermissionOverride(role)
                .getDenied();
        if (!threadPermissions.contains(permission)) {
            threadChannel.createPermissionOverride(role)
                .setDeny(permission)
                .queue();
        }
    }

    private static TextChannel findThreadTextChannel(Channel threadChannel, JDA jda) {
        return TextChannelUtil.getChannel(threadChannel.getId(), jda);
    }

    private static void sendTopicHasBeenSetMsg(TextChannel threadTextChannel, String topic, CommandEvent event) {
        User user = event.getAuthor();
        EmbedBuilder builder = new EmbedBuilder()
            .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
            .setDescription("The topic has now been set to: " +
                String.format("**%s**", topic));
        List<Attachment> attachments = event.getMessage().getAttachments();
        if (!attachments.isEmpty()) {
            builder.setImage(attachments.get(0).getUrl());
        }
        threadTextChannel.sendMessage(builder.build())
            .queue(msg -> {
                long threadId = threadTextChannel.getIdLong();
                ThreadDbTable.storeLatestMsgId(
                    msg.getIdLong(), threadId);
                ThreadDbTable.storePostCount(0, threadId);
                msg.pin().queue();
            });
    }
}