package commands.thread;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import database.ThreadDbInfo;
import database.ThreadDbTable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import utils.ArgumentChecker;
import utils.CategoryUtil;

public class DeleteThreadCommand extends Command {
    private static final int RESPONSE_TIMEOUT_IN_SEC = 35;
    private final EventWaiter waiter;

    public DeleteThreadCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "deletethread";
        this.aliases = new String[] { "delthread" };
        this.help = "choose a created thread to delete.";
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        ThreadDbInfo userThreadInfo = ThreadDbTable.getThreadInfoFromUser(event.getMember().getUser());
        if (args.isEmpty()) {
            event.reply(String.format("Listing created threads for %s: %n",
                event.getMessage().getAuthor().getAsMention()));
            event.reply(userThreadInfo.getlistedChannels());
            if (!userThreadInfo.getThreadIds().isEmpty()) {
                promptUser(userThreadInfo, event);
            }
        } else {
            try {
                TextChannel threadToDelete = getThreadToDeleteByName(args, event.getJDA());
                userThreadInfo.getThreadIds().stream()
                    .filter(id -> id == threadToDelete.getIdLong())
                    .findFirst()
                    .orElseThrow(() -> createThreadNotFoundException(args));
                deleteChannel(event, threadToDelete);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                event.replyWarning(ex.getMessage());
            }
        }
    }

    private void promptUser(ThreadDbInfo userThreadInfo, CommandEvent event) {
        event.reply("Please type in the number of the thread you want to delete.");
        waiter.waitForEvent(MessageReceivedEvent.class,
            e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()),
            e -> {
                String msgWithNumber = e.getMessage().getContentRaw();
                try {
                    validateInput(msgWithNumber, userThreadInfo.getThreadIds().size());
                    TextChannel threadToDelete =
                        getThreadToDeleteByNumber(event.getJDA(), msgWithNumber, userThreadInfo);
                    deleteChannel(event, threadToDelete);
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    event.replyWarning(ex.getMessage());
                }
            },
            RESPONSE_TIMEOUT_IN_SEC, TimeUnit.SECONDS, () -> event.reply(String.format("Sorry %s, you took too long.",
                event.getMessage().getAuthor().getAsMention())));
    }

    private void deleteChannel(CommandEvent event, TextChannel threadToDelete) {
        try {
            if (!event.getChannel().getId().equals(threadToDelete.getId())) {
                handleDeletionOfThread(threadToDelete);
                String channelName = threadToDelete.getName();
                event.reply(String.format("Successfully deleted thread: **%s**",
                    channelName));
            } else {
                event.replyError("Cannot delete the channel you are already in!\n"
                    + "Please retry this command in **#botspam**");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            event.replyWarning(e.getMessage() + "\n" +
                String.format("%s Please try running the %s command again",
                    event.getMessage().getAuthor().getAsMention(),
                    event.getClient().getPrefix() + name));
        }
    }

    private void handleDeletionOfThread(TextChannel threadToDelete) {
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

    private TextChannel getThreadToDeleteByName(String args, JDA jda) {
        return CategoryUtil.getThreadCategory(jda)
            .getTextChannels()
            .stream()
            .filter(thread -> thread.getName().replaceAll("[\u2004]", " ")
                .equals(args))
            .findFirst()
            .orElseThrow(() -> createThreadNotFoundException(args));
    }

    private TextChannel getThreadToDeleteByNumber(JDA jda, String number, ThreadDbInfo threadInfo) {
        long threadId = threadInfo.getThreadIds()
            .get(Integer.valueOf(number) - 1);
        return CategoryUtil.getThreadCategory(jda).getTextChannels().stream()
            .filter(thread -> thread.getIdLong() == threadId)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Could not find thread to delete!"));
    }

    private void validateInput(String args, int numberOfchannels) {
        ArgumentChecker.checkArgsBySpaceRequires(args, 1);
        Preconditions.checkArgument(StringUtils.isNumeric(args),
            String.format("Invalid thread id \"%s\", id must be numeric", args));
        Preconditions.checkArgument(Integer.valueOf(args) != 0,
            "ID must be greater than zero!");
        Preconditions.checkArgument(Integer.valueOf(args) <= numberOfchannels,
            String.format("No thread found with ID #%s", args));
    }

    private IllegalStateException createThreadNotFoundException(String threadName) {
        return new IllegalStateException(String.format("You have no thread with name \"**%s**\"", threadName));
    }
}
