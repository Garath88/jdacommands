package commands.channel.thread;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.channel.database.ChannelDbInfo;
import commands.channel.database.ThreadsDbTable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import utils.ArgumentChecker;
import utils.CategoryUtil;
import utils.GuildUtil;
import utils.MessageUtil;
import utils.StreamUtil;

public class DeleteThreadCommand extends Command {
    private static final int RESPONSE_TIMEOUT_IN_SEC = 35;
    public static EventWaiter waiter = null;
    private static String commandName = "";

    public DeleteThreadCommand(EventWaiter waiter) {
        DeleteThreadCommand.waiter = waiter;
        this.name = "deletethread";
        commandName = name;
        this.aliases = new String[] { "delthread" };
        this.help = "Choose a created thread to delete.";
        this.guildOnly = false;
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        JDA jda = event.getJDA();
        Member member = GuildUtil.getGuild(jda).getMember(event.getAuthor());
        if (member != null) {
            ChannelDbInfo userThreadInfo = ThreadsDbTable.getThreadInfoFromUser(member.getUser());
            showCreatedChannelsToDelete(event, userThreadInfo, CategoryUtil.getThreadCategory(jda));
        }
    }

    public static void showCreatedChannelsToDelete(CommandEvent event, ChannelDbInfo channelDbInfo,
        net.dv8tion.jda.api.entities.Category category) {

        String args = event.getArgs();
        if (args.isEmpty()) {
            event.reply(String.format("Listing created channels for %s: %n",
                event.getMessage().getAuthor().getAsMention()));
            event.reply(channelDbInfo.getlistedChannels());
            if (!channelDbInfo.getThreadIds().isEmpty()) {
                promptUser(channelDbInfo, event, category);
            }
        } else {
            try {
                TextChannel threadToDelete = getThreadToDeleteByName(args, event.getJDA(), category);
                channelDbInfo.getThreadIds().stream()
                    .filter(id -> id == threadToDelete.getIdLong())
                    .findFirst()
                    .orElseThrow(() -> createThreadNotFoundException(args));
                deleteChannel(event, threadToDelete);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                event.replyWarning(ex.getMessage());
            }
        }
    }

    private static void promptUser(ChannelDbInfo userThreadInfo, CommandEvent event,
        net.dv8tion.jda.api.entities.Category category) {

        event.reply("Please type in the number of the channel you want to delete:");
        MessageUtil.waitForResponseInChannel(event, waiter, e -> {
                String msgWithNumber = e.getMessage().getContentRaw();
                try {
                    validateInput(msgWithNumber, userThreadInfo.getThreadIds().size());
                    TextChannel threadToDelete =
                        getThreadToDeleteByNumber(msgWithNumber, userThreadInfo, category);
                    deleteChannel(event, threadToDelete);
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    event.replyWarning(ex.getMessage());
                }
            },
            RESPONSE_TIMEOUT_IN_SEC, String.format("Sorry %s, you were too slow to respond.",
                event.getMessage().getAuthor().getAsMention()));
    }

    private static void deleteChannel(CommandEvent event, TextChannel threadToDelete) {
        try {
            if (!event.getChannel().getId().equals(threadToDelete.getId())) {
                handleDeletionOfThread(threadToDelete);
                String channelName = threadToDelete.getName();
                event.reply(String.format("Successfully deleted channel: **%s**",
                    channelName));
            } else {
                event.replyError("Cannot delete the channel you are already in!\n"
                    + "Please retry this command in **#botspam**");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            event.replyWarning(e.getMessage() + "\n" +
                String.format("%s Please try running the %s command again",
                    event.getMessage().getAuthor().getAsMention(),
                    event.getClient().getPrefix() + commandName));
        }
    }

    private static void handleDeletionOfThread(TextChannel threadToDelete) {
        Optional<InactiveThreadCheckTask> threadTaskToBeDeleted =
            InactiveThreadChecker.getThreadTask(threadToDelete.getIdLong());
        if (threadTaskToBeDeleted.isPresent()) {
            InactiveThreadCheckTask task = threadTaskToBeDeleted.get();
            task.deleteChannel();
        } else {
            threadToDelete.delete()
                .queue();
        }
    }

    private static TextChannel getThreadToDeleteByName(String args, JDA jda,
        net.dv8tion.jda.api.entities.Category channelCategory) {

        String temp = args.replaceAll(" ", "\u2004");
        return FinderUtil.findTextChannels(temp, GuildUtil.getGuild(jda)).stream()
            .filter(channelCategory.getTextChannels()::contains)
            .reduce(StreamUtil.toOnlyElementThrowing(() ->
                new IllegalStateException("Found more than one channel to delete!"))).
                orElseThrow(() -> createThreadNotFoundException(args));
    }

    private static TextChannel getThreadToDeleteByNumber(String number, ChannelDbInfo threadInfo,
        net.dv8tion.jda.api.entities.Category category) {

        long threadId = threadInfo.getThreadIds()
            .get(Integer.valueOf(number) - 1);
        return category.getTextChannels().stream()
            .filter(thread -> thread.getIdLong() == threadId)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                String.format("Could not find the channel to delete in category %s",
                    category.getName())));
    }

    private static void validateInput(String args, int numberOfchannels) {
        ArgumentChecker.checkArgsBySpaceRequires(args, 1);
        Preconditions.checkArgument(StringUtils.isNumeric(args),
            String.format("Invalid id \"%s\", input must be numeric.", args));
        Preconditions.checkArgument(Integer.valueOf(args) != 0,
            "ID must be greater than zero!");
        Preconditions.checkArgument(Integer.valueOf(args) <= numberOfchannels,
            String.format("No channel found with ID #%s", args));
    }

    private static IllegalStateException createThreadNotFoundException(String threadName) {
        return new IllegalStateException(String.format("You have no channel with name \"**%s**\"", threadName));
    }
}
