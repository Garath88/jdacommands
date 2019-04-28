package commands.thread.prompt;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.CommandEvent;
import commands.thread.ThreadInfo;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ThreadDescriptionResponse implements Consumer<MessageReceivedEvent> {

    private CommandEvent event;
    private String name;
    private BiConsumer<CommandEvent, ThreadInfo> method;

    ThreadDescriptionResponse(CommandEvent event, String name, BiConsumer<CommandEvent, ThreadInfo> method) {
        this.event = event;
        this.name = name;
        this.method = method;
    }

    @Override
    public void accept(MessageReceivedEvent e) {
        method.accept(event, new ThreadInfo(name, e.getMessage()
            .getContentRaw()
            .toLowerCase(), true));
    }
}