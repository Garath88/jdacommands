package commands.quiz;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import utils.PrivateChannelWrapper;

class RulesMessage {
    static final int TIME_TO_READ_RULES_IN_SEC = 7;

    private RulesMessage() {
    }

    static void perform(User user, Guild guild, EventWaiter waiter, CommandClient client, String rulesChannel) {
        user.openPrivateChannel()
            .queueAfter(15, TimeUnit.SECONDS,
                PrivateChannelWrapper.userIsInGuild(pc -> pc.sendMessage(
                    String.format("- OH! I almost forgot!%n- You should go and read the rules in %s!",
                        rulesChannel))
                    .queue(msg2 -> RulesQuestion.perform(TIME_TO_READ_RULES_IN_SEC,
                        user, guild, waiter, client),
                        fail -> {
                        })),
                fail -> {
                });
    }
}