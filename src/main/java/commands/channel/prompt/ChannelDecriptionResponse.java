package commands.channel.prompt;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.CommandEvent;

import commands.channel.ChannelInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChannelDecriptionResponse implements Consumer<MessageReceivedEvent> {

    private CommandEvent event;
    private String name;
    private BiConsumer<CommandEvent, ChannelInfo> createChannelMethod;

    ChannelDecriptionResponse(CommandEvent event, String name, BiConsumer<CommandEvent, ChannelInfo> createChannelMethod) {
        this.event = event;
        this.name = name;
        this.createChannelMethod = createChannelMethod;
    }

    @Override
    public void accept(MessageReceivedEvent e) {
        createChannelMethod.accept(event, new ChannelInfo(name, e.getMessage()
            .getContentRaw(), true));
    }
}