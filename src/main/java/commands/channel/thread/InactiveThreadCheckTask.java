package commands.channel.thread;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import commands.channel.database.ThreadsDbTable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import tasks.Task;
import utils.CategoryUtil;

public final class InactiveThreadCheckTask extends Task {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(InactiveThreadCheckTask.class);
    private static final int NO_CONTENT_EXPIRE_AFTER_MIN = 6;
    private static final int EXPIRE_AFTER_MIN = 48 * 60;
    private static final int WARNING_BEFORE_MIN = 8 * 60;
    private static final int FINAL_WARNING_BEFORE_MIN = 60;
    private final TextChannel thread;
    private OffsetDateTime deleteTime = null;
    private boolean hasWarned;

    InactiveThreadCheckTask(TextChannel thread) {
        super((long)(NO_CONTENT_EXPIRE_AFTER_MIN / 1.5),
            (long)(NO_CONTENT_EXPIRE_AFTER_MIN / 1.5));
        this.thread = thread;
    }

    private InactiveThreadCheckTask(TextChannel thread,
        long loopTime, long delay, OffsetDateTime deleteTime) {
        super(loopTime, delay);
        this.thread = thread;
        hasWarned = true;
        this.deleteTime = deleteTime;
    }

    @Override
    public void execute() {
        checkIfChannelIsInactive();
    }

    private void checkIfChannelIsInactive() {
        long timeLeft = getExpirationTimeInMinutes();
        String debug = String.format("Thread: %s - timeleft: %s",
            thread.getName(), timeLeft);
        LOGGER.debug(debug);
        try {
            if (timeLeft <= 0) {
                if (!hasWarned) {
                    deleteTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(EXPIRE_AFTER_MIN);
                    sendInactivityMsg(EXPIRE_AFTER_MIN);
                    scheduleDelete(WARNING_BEFORE_MIN, 1, deleteTime);
                } else {
                    moveChannelToPublicArchive();
                }
            } else if (timeLeft <= FINAL_WARNING_BEFORE_MIN) {
                sendInactivityMsg(timeLeft);
                scheduleDelete(timeLeft, 0, deleteTime);
            } else if (timeLeft <= WARNING_BEFORE_MIN) {
                sendInactivityMsg(timeLeft);
                scheduleDelete(timeLeft, FINAL_WARNING_BEFORE_MIN, deleteTime);
            } else {
                scheduleDelete(timeLeft, WARNING_BEFORE_MIN, deleteTime);
            }
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    private void sendInactivityMsg(long expirationTime) {
        long threadId = thread.getIdLong();
        int currentBumpCount = ThreadsDbTable.getBumpCount(threadId) + 1;
        if (currentBumpCount < 35) {
            LOGGER.debug("Sending inactivity message to channel {}",
                thread.getName());
            String message = createInactivityMessage(expirationTime);
            thread.sendMessage(message).queue(
                success -> ThreadsDbTable.storeBumpCount(currentBumpCount, threadId));
        } else {
            moveThreadToPublicArchive();
        }
    }

    private void moveThreadToPublicArchive() {
        thread.getManager().setParent(CategoryUtil.getPublicArchiveCategory(thread.getJDA()))
            .queue(success -> thread.getManager().sync().queue());
    }

    private void scheduleDelete(long timeLeft, long warningTime, OffsetDateTime deleteTime) {
        reScheduleTask(new InactiveThreadCheckTask(thread,
            1, timeLeft - warningTime, deleteTime));
    }

    private List<Message> getLatestMessages() {
        return thread.getIterableHistory().stream()
            .limit(5)
            .collect(Collectors.toList());
    }

    private long getExpirationTimeInMinutes() {
        OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC);

        List<Message> latestMessages = getLatestMessages();
        OffsetDateTime latestMessageTime = getLatestMessageTime(latestMessages);

        if (latestMessageTime != null) {
            long timeLeft = currentTime.until(latestMessageTime.plusMinutes(EXPIRE_AFTER_MIN),
                ChronoUnit.MINUTES);
            if (timeLeft > 0 || deleteTime == null) {
                return timeLeft;
            } else {
                return currentTime.until(deleteTime,
                    ChronoUnit.MINUTES);
            }
        } else {
            return currentTime.until(thread.getTimeCreated()
                    .plusMinutes(NO_CONTENT_EXPIRE_AFTER_MIN),
                ChronoUnit.MINUTES);
        }
    }

    private OffsetDateTime getLatestMessageTime(List<Message> latestMessages) {
        Message latestUserMsg = getLatestUserMessage(latestMessages);
        if (latestUserMsg != null) {
            return latestUserMsg.getTimeCreated();
        }
        return null;
    }

    private Message getLatestUserMessage(List<Message> messages) {
        List<Message> latestUserMsgs = messages.stream()
            .filter(msg -> !msg.getAuthor().isBot())
            .collect(Collectors.toList());
        if (!latestUserMsgs.isEmpty()) {
            return latestUserMsgs.get(0);
        }
        return null;
    }

    private void moveChannelToPublicArchive() {
        InactiveThreadChecker.getTaskListContainer()
            .cancelTask(this);
        if (InactiveThreadChecker.shouldNotBeSaved(thread)) {
            moveThreadToPublicArchive();
        }
    }

    private String createInactivityMessage(long expirationTime) {
        long hours = expirationTime / 60;
        long minutes = expirationTime % 60;
        return String.format("This thread has been marked as **inactive** "
            + "and will be deleted in **%dh:%02dm** if no activity occurs!", hours, minutes);
    }

    private void reScheduleTask(Task newTask) {
        InactiveThreadChecker.getTaskListContainer()
            .reScheduleTask(this,
                newTask);
    }

    public TextChannel getThread() {
        return thread;
    }
}