package commands.channel.thread;

import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.Permissions;
import commands.channel.ChannelInfo;
import commands.channel.database.ChannelDbInfo;
import commands.channel.database.ThreadsDbTable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import utils.CategoryUtil;
import utils.GuildUtil;
import utils.RoleUtil;
import utils.TextChannelUtil;

public class ThreadCommand extends Command {
    private static final int MAX_AMOUNT_OF_THREADS = 15;
    private static final int LURKER_MAX_THREAD_LIMIT = 1;
    private final EventWaiter waiter;

    public ThreadCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "thread";
        this.help = "Creates a new thread with description.";
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
            TextChannelUtil.addNewThread(event, waiter, ThreadCommand::createThreadChannel);
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
            ChannelDbInfo threadInfo = ThreadsDbTable.getThreadInfoFromUser(event.getAuthor());
            return threadInfo.getThreadIds().size() >= LURKER_MAX_THREAD_LIMIT;
        }
        return false;
    }

    public static void createThreadChannel(CommandEvent event, ChannelInfo threadInfo) {
        net.dv8tion.jda.api.entities.Category threadCategory = CategoryUtil.getThreadCategory(event.getJDA());
        String name = threadInfo.getName();
        String description = threadInfo.getDescription();
        validateThreadName(threadCategory, name);
        final String channelTopic = name.replaceAll(" ", "` `");
        GuildUtil.getGuild(event.getJDA()).createTextChannel(channelTopic)
            .setTopic(description)
            .setNSFW(true)
            .setParent(threadCategory)
            .queue(channel -> doTasks(channel, event, description, threadInfo.getStoreInDatabase()));
    }

    private static void validateThreadName(net.dv8tion.jda.api.entities.Category customCategory, String topic) {
        if (customCategory.getTextChannels().stream().anyMatch(chan -> chan.getName().equals(topic))) {
            throw new IllegalArgumentException(String.format(
                "This thread **%s** already exists! Please retry the command again.", topic));
        }
    }

    private static void doTasks(TextChannel threadChannel, CommandEvent event, String topic, boolean storeInDatabase) {
        TextChannelUtil.blockQuizUsersForChannel(threadChannel, event);
        TextChannel threadTextChannel = TextChannelUtil.getChannel(threadChannel.getId(), event.getJDA());
        if (storeInDatabase) {
            ThreadsDbTable.addThread(event.getAuthor(), threadChannel);
            sendTopicHasBeenSetMsg(threadTextChannel, topic, event);
            InactiveThreadChecker.startOrCancelInactivityTaskIfNotTopX(threadTextChannel);
        } else { //Sakura thread
            ThreadsDbTable.addThread(event.getSelfUser(), threadChannel);
            ThreadsDbTable.storePostCount(9999,
                threadChannel.getIdLong());
        }
        event.reply(String.format("Successfully created new thread: **%s**", threadTextChannel.getAsMention()));
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
                ThreadsDbTable.storeLatestMsgId(
                    msg.getIdLong(), threadId);
                ThreadsDbTable.storePostCount(0, threadId);
                msg.pin().queue();
            });
    }
}