package commands.bump;

import com.jagrosh.jdautilities.command.CommandEvent;

import tasks.Task;

final class BumpTask extends Task {
    private final String commandToBump;
    private final CommandEvent event;

    BumpTask(String commandToBump, int loopTime, CommandEvent event) {
        super(loopTime, 0);
        this.commandToBump = commandToBump;
        this.event = event;
    }

    @Override
    public void execute() {
        event.reply(commandToBump);
    }
}
