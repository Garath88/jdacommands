package commands.channel.prompt;

import java.util.function.BiConsumer;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.channel.ChannelInfo;
import utils.MessageUtil;

public final class ChannelQuestion {
    private ChannelQuestion() {
    }

    public static void perform(CommandEvent event, EventWaiter waiter, BiConsumer<CommandEvent, ChannelInfo> func) {
        event.reply(String.format("What **name** do you want for the channel? %s",
            event.getAuthor()));
        MessageUtil.waitForResponseInChannel(
            event, waiter, new ChannelNameResponse(event, waiter, func), 60,
            "");
    }
}