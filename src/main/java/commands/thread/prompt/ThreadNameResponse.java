package commands.thread.prompt;

import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.thread.ThreadCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utils.MessageUtil;

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
        try {
            ThreadCommand.validateName(name, event);
            event.reply(String.format("Great! Now type in a **description** for the thread %s",
                event.getAuthor()));
            MessageUtil.waitForResponseInChannel(event, waiter,
                new ThreadDescriptionResponse(event, name, ThreadCommand::createNewThread), 2 * 60,
                "");
        } catch (IllegalArgumentException ex) {
            event.replyWarning(String.format("%s %s",
                event.getMessage().getAuthor().getAsMention(), ex.getMessage()));
        }
    }
}