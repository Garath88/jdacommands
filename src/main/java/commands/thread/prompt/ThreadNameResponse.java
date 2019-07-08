package commands.thread.prompt;

import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import commands.thread.ThreadCommand;
import utils.MessageUtil;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ThreadNameResponse implements Consumer<MessageReceivedEvent> {

    private CommandEvent event;
    private final EventWaiter waiter;

    ThreadNameResponse(CommandEvent event, EventWaiter waiter) {
        this.event = event;
        this.waiter = waiter;
    }

    @Override
    public void accept(MessageReceivedEvent e) {
        String name = e.getMessage().getContentRaw()
            .toLowerCase();
        event.reply("Please type in a **description** for the thread:");
        MessageUtil.waitForResponseInChannel(event, waiter,
            new ThreadDescriptionResponse(event, name, ThreadCommand::createNewThread), 2,
            "");
    }
}