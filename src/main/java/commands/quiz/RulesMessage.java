package commands.quiz;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import utils.PrivateChannelWrapper;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

class RulesMessage {
    private static final String RULES_CHANNEL = "server info";
    static final int TIME_TO_READ_RULES_IN_SEC = 7;

    private RulesMessage() {
    }

    static void perform(User user, Guild guild, EventWaiter waiter, CommandClient client) {
        TextChannel rulesChannel = guild.getTextChannelsByName(
            RULES_CHANNEL, true).stream()
            .findFirst()
            .orElseThrow(IllegalStateException::new);
        user.openPrivateChannel()
            .queueAfter(1, TimeUnit.SECONDS,
                PrivateChannelWrapper.userIsInGuild(pc -> pc.sendMessage(
                    String.format("- You should go and read the rules in %s",
                        rulesChannel.getAsMention()))
                    .queue(msg2 -> RulesQuestion.perform(TIME_TO_READ_RULES_IN_SEC,
                        user, guild, waiter, client))),
                fail -> {
                });
    }
}