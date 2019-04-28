package commands.thread;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tasks.TaskListContainer;
import utils.CategoryUtil;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;

public final class InactiveThreadChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(InactiveThreadChecker.class);
    private static final int MIN_POS_TO_SAVE_CHAN = 6;
    private static TaskListContainer taskListContainer = new TaskListContainer();

    private InactiveThreadChecker() {
    }

    static TaskListContainer getTaskListContainer() {
        return taskListContainer;
    }

    public static void startOrCancelInactivityTaskIfNotTopX(JDA jda) {
        List<TextChannel> allThreads = CategoryUtil.getThreadCategory(jda).getTextChannels();
        allThreads.forEach(InactiveThreadChecker::startOrCancelInactivityTaskIfNotTopX);
    }

    static void startOrCancelInactivityTaskIfNotTopX(TextChannel textChannel) {
        boolean lowPosChannel = shouldNotBeSaved(textChannel);
        if (lowPosChannel && notAlreadyRunning(textChannel.getId())) {
            taskListContainer.addTask(new InactiveThreadCheckTask(textChannel));
            taskListContainer.scheduleTasks();
        } else if (!lowPosChannel) {
            cancelInactivityTask(textChannel);
        }
    }

    static boolean shouldNotBeSaved(TextChannel textChannel) {
        List<TextChannel> allThreads = CategoryUtil.getThreadCategory(textChannel.getJDA()).getTextChannels();
        return allThreads.indexOf(textChannel) >= MIN_POS_TO_SAVE_CHAN;
    }

    private static boolean notAlreadyRunning(String id) {
        return getTasks().stream()
            .noneMatch(task -> task.getThread().getId().equals(id));
    }

    private static void cancelInactivityTask(TextChannel textChannel) {
        Optional<InactiveThreadCheckTask> task = getThreadTask(textChannel.getIdLong());
        //*TODO: REMOVE EXTRA LOGGING:
        if (task.isPresent()) {
            String debugInfo = String.format("Canceling inactivity task for %s",
                textChannel.getName());
            LOGGER.info(debugInfo);
        }
        task.ifPresent(inactiveThreadCheckTask ->
            taskListContainer.cancelTask(inactiveThreadCheckTask));
    }

    public static void cancelTaskIfDeleted(TextChannel thread) {
        if (threadHasBeenDeleted(thread)) {
            Optional<InactiveThreadCheckTask> task = getThreadTask(thread.getIdLong());
            task.ifPresent(inactiveThreadCheckTask ->
                InactiveThreadChecker.getTaskListContainer()
                    .cancelTask(inactiveThreadCheckTask));
        }
    }

    private static boolean threadHasBeenDeleted(TextChannel thread) {
        return thread.getJDA().getTextChannels().stream()
            .noneMatch(chan -> chan.getId().equals(thread.getId()));
    }

    private static List<InactiveThreadCheckTask> getTasks() {
        return taskListContainer.getTaskList()
            .stream()
            .map(task -> (InactiveThreadCheckTask)task)
            .collect(Collectors.toList());
    }

    static Optional<InactiveThreadCheckTask> getThreadTask(Long id) {
        return InactiveThreadChecker.getTasks()
            .stream()
            .filter(task -> task.getThread().getIdLong() == (id))
            .findFirst();
    }
}
