package commands.quiz;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import utils.MessageUtil;
import utils.PrivateChannelWrapper;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

final class RulesQuestion {
    private static final int RULES_TIMEOUT_IN_MIN = 6;

    private RulesQuestion() {
    }

    static void perform(int delay, User user, Guild guild, EventWaiter waiter, CommandClient client) {
        user.openPrivateChannel()
            .queueAfter(delay, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(pc ->
                pc.sendMessage("- Have you read the rules? **(yes/no)**")
                    .queue(listen -> MessageUtil.waitForResponseInDM(user, guild, waiter,
                        new RulesResponse(client), RULES_TIMEOUT_IN_MIN,
                        new QuizResponse(client).getRetryMessage()),
                        fail -> {
                        })));
    }
}
