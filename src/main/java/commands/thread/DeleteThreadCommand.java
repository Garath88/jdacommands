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
        this.help = "choose a created thread to delete.";
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            ArgumentChecker.checkArgsBySpace(event.getArgs(), 0);
            ThreadDbInfo threadInfo = ThreadDbTable.getThreadInfoFromUser(event.getMember().getUser());
            event.reply(String.format("Listing created threads for %s: %n",
                event.getMessage().getAuthor().getAsMention()));
            event.reply(threadInfo.getlistedChannels());
            if (!threadInfo.getThreadIds().isEmpty()) {
                event.reply("Please type in the number of the thread you want to delete.");
                // wait for a response
                waiter.waitForEvent(MessageReceivedEvent.class,
                    // make sure it's by the same user, and in the same channel
                    e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()),
                    // respond, inserting the name they listed into the response
                    e -> deleteChannel(event, e.getMessage().getContentRaw(), threadInfo),
                    // if the user takes more than a minute, time out
                    RESPONSE_TIMEOUT_IN_SEC, TimeUnit.SECONDS, () -> event.reply(String.format("Sorry %s, you took too long.",
                        event.getMessage().getAuthor().getAsMention())));
            }
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private void deleteChannel(CommandEvent event, String msgWithNumber,
        ThreadDbInfo threadInfo) {
        try {
            validateInput(msgWithNumber, threadInfo.getThreadIds().size());
            TextChannel threadToDelete =
                getThreadToDelete(event.getJDA(), msgWithNumber, threadInfo);
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

    private TextChannel getThreadToDelete(JDA jda, String number, ThreadDbInfo threadInfo) {
        long threadId = threadInfo.getThreadIds()
            .get(Integer.valueOf(number) - 1);
        return CategoryUtil.getThreadCategory(jda).getTextChannels().stream()
            .filter(thread -> thread.getIdLong() == threadId)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Could not find thread to delete!"));
    }

    private void validateInput(String args, int numberOfchannels) {
        ArgumentChecker.checkArgsBySpace(args, 1);
        Preconditions.checkArgument(StringUtils.isNumeric(args),
            String.format("Invalid thread id \"%s\", id must be numeric", args));
        Preconditions.checkArgument(Double.parseDouble(args) != 0,
            "ID must be greater than zero!");
        Preconditions.checkArgument(Integer.valueOf(args) <= numberOfchannels,
            String.format("No thread found with ID #%s", args));
    }
}
