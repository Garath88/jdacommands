package commands.quiz;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import utils.MessageUtil;
import utils.PrivateChannelWrapper;

final class RulesQuestion {
    private static final int RULES_TIMEOUT_IN_SECONDS = 6 * 60;

    private RulesQuestion() {
    }

    static void perform(int delay, User user, Guild guild, EventWaiter waiter, CommandClient client) {
        user.openPrivateChannel()
            .queueAfter(delay, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(pc ->
                pc.sendTyping().queueAfter(1000, TimeUnit.MILLISECONDS,
                    PrivateChannelWrapper.userIsInGuild(
                        msg -> pc.sendMessage("- Have you read the rules? **(yes/no)**")
                            .queue(listen -> MessageUtil.waitForResponseInDM(user, guild, waiter,
                                new RulesResponse(client), RULES_TIMEOUT_IN_SECONDS,
                                new QuizResponse(client).getRetryMessage(), client),
                                fail -> {
                                })),
                    fail -> {
                    })
            ), fail -> {
            });
    }
}