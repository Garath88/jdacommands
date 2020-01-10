package commands.quiz;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import utils.TriFunction;
import utils.PrivateChannelWrapper;
import utils.RoleUtil;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class RulesResponse implements TriFunction<Guild, MessageReceivedEvent, EventWaiter> {
    private CommandClient client;

    RulesResponse(CommandClient client) {
        this.client = client;
    }

    @Override
    public void apply(Guild guild, MessageReceivedEvent e, EventWaiter waiter) {
        User user = e.getAuthor();
        String response = e.getMessage().getContentRaw().toLowerCase();
        if (response.equals("yes") || response.equals("y")) {
            RoleUtil.removeRole(guild, user, QuizQuestion.RULES_ROLE);
            user.openPrivateChannel().queue(PrivateChannelWrapper.userIsInGuild(pc -> pc.sendMessage(
                "- Awesome! Welcome!").queue(
                PrivateChannelWrapper.userIsInGuild(msg2 -> pc.sendMessage(
                    "- OH! I almost forgot!\n- Here's stuff that I currently can do:").queueAfter(12, TimeUnit.SECONDS,
                    PrivateChannelWrapper.userIsInGuild(msg3 -> client.displayHelp(new CommandEvent(e, null, client))))),
                fail -> {
                })),
                fail -> {
                });
        } else {
            RulesQuestion.perform(RulesMessage.TIME_TO_READ_RULES_IN_SEC + 5,
                user, guild, waiter, client);
        }
    }
}
