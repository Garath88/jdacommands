package commands.bump;

import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import tasks.Task;

public final class BumpCommand extends Command {
    private static boolean running;

    public BumpCommand() {
        this.name = "bump";
        this.help = "repeat commands periodically.";
        this.requiredRoles = Permissions.MODERATOR.getValues();
    }

    static boolean isRunning() {
        return running;
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Task> bumpTasks = BumpTaskList.getTaskListContainer().
            getTaskList();
        if (bumpTasks.isEmpty()) {
            event.replyWarning(
                "You haven't added any commands to bump! \n "
                    + "Please use the **" + event.getClient().getPrefix() + "bump_add** command");
        } else {
            List<Task> queuedTasks = BumpTaskList.getTaskListContainer()
                .getQueuedTasks();
            if (queuedTasks.isEmpty()) {
                stopBump(event);
            } else {
                scheduleBump(event);
            }
        }
    }

    static void scheduleBump(CommandEvent event) {
        if (!running) {
            event.reply("**Starting bumping..** \n"
                + "_I'll make ya bump hump wiggle and shake your rump!_");
        }
        running = true;
        BumpTaskList.getTaskListContainer().
            scheduleTasks();
    }

    private static void stopBump(CommandEvent event) {
        event.reply("**Stopping bumping..** \n"
            + "_I got you jumpin' an' bumpin' an' pumpin' movin' all around, G_");
        running = false;
        BumpTaskList.getTaskListContainer().
            cancelTasks();
    }
}
