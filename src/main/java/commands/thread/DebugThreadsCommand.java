package commands.thread;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.core.Permission;

public class DebugThreadsCommand extends Command {

    public DebugThreadsCommand() {
        this.name = "debugthreads";
        this.help = "debug sorting and deletion of threads";
        this.guildOnly = false;
        this.ownerCommand = true;
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        SortThreads.sortAllThreadsByPostCountAndStartOrCancelInactivityTask(event.getJDA());
    }
}
