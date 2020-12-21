package commands.channel.prompt;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.channel.ChannelInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utils.MessageUtil;
import utils.TextChannelUtil;

public class ChannelNameResponse implements Consumer<MessageReceivedEvent> {

    private final BiConsumer<CommandEvent, ChannelInfo> createChannelMethod;
    private CommandEvent event;
    private final EventWaiter waiter;

    ChannelNameResponse(CommandEvent event, EventWaiter waiter, BiConsumer<CommandEvent, ChannelInfo> createChannelMethod) {
        this.event = event;
        this.waiter = waiter;
        this.createChannelMethod = createChannelMethod;
    }

    @Override
    public void accept(MessageReceivedEvent e) {
        String name = e.getMessage().getContentRaw()
            .toLowerCase();
        try {
            TextChannelUtil.validateName(name, event);
            event.reply(String.format("Great! Now type in a **description** for the channel %s",
                event.getAuthor()));
            MessageUtil.waitForResponseInChannel(event, waiter,
                new ChannelDecriptionResponse(event, name, (event, threadInfo) ->
                    TextChannelUtil.createNewThread(event, threadInfo,
                        createChannelMethod)), 2 * 60,
                "");
        } catch (IllegalArgumentException ex) {
            event.replyWarning(String.format("%s %s",
                event.getMessage().getAuthor().getAsMention(), ex.getMessage()));
        }
    }
}