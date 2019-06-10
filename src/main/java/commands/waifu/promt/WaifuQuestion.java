package commands.waifu.promt;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.waifu.Roles;
import utils.MessageUtil;

public final class WaifuQuestion {
    private WaifuQuestion() {
    }

    public static void perform(CommandEvent event, EventWaiter waiter, Roles roles) {
        event.reply("Please type in the **name** of your waifu");
        MessageUtil.waitForResponseInChannel(
            event, waiter, new WaifuResponse(event, waiter, roles), 1,
            "");
    }
}
