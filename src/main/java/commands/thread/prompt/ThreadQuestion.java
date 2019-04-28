package commands.thread.prompt;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import utils.MessageUtil;

public final class ThreadQuestion {
    private ThreadQuestion() {
    }

    public static void perform(CommandEvent event, EventWaiter waiter) {
        event.reply("Please type in the **name** for the thread:");
        MessageUtil.waitForResponseInChannel(
            event, waiter, new ThreadNameResponse(event, waiter), 1,
            "");
    }
}