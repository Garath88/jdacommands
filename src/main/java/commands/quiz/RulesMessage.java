package commands.quiz;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import utils.PrivateChannelWrapper;

class RulesMessage {
    static final int TIME_TO_READ_RULES_IN_SEC = 7;

    private RulesMessage() {
    }

    static void perform(User user, Guild guild, EventWaiter waiter, CommandClient client, String rulesChannel) {
        user.openPrivateChannel()
            .queueAfter(15, TimeUnit.SECONDS,
                PrivateChannelWrapper.userIsInGuild(pc -> pc.sendMessage(
                    "- OH! I almost forgot!")
                    .queue(PrivateChannelWrapper.userIsInGuild(msg2 -> {
                            pc.sendTyping().queue();
                            pc.sendMessage(String.format("- You should read the rules in %s", rulesChannel))
                                .queueAfter(1000, TimeUnit.MILLISECONDS,
                                    PrivateChannelWrapper.userIsInGuild(
                                        msg3 -> RulesQuestion.perform(TIME_TO_READ_RULES_IN_SEC,
                                            user, guild, waiter, client)),
                                    fail -> {
                                    });
                        }),
                        fail -> {
                        })),
                fail -> {
                });
    }
}