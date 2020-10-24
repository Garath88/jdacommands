package commands.quiz;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import utils.PrivateChannelWrapper;

class HelpMessage {
    private static final String RULES_CHANNEL = "server info";

    private HelpMessage() {
    }

    static void perform(User user, Guild guild, EventWaiter waiter, CommandClient client, MessageReceivedEvent e) {
        String rulesChannel = guild.getTextChannelsByName(
            RULES_CHANNEL, true).stream()
            .map(IMentionable::getAsMention)
            .findFirst()
            .orElseThrow(IllegalStateException::new);

        user.openPrivateChannel()
            .queueAfter(2, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(pc -> {
                    pc.sendTyping().queue();
                    pc.sendMessage("- Here's stuff that I currently can do:")
                        .queueAfter(4, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(msg2 -> {
                                pc.sendTyping().queue();
                                client.displayHelp(new CommandEvent(e, null, client));
                                RulesMessage.perform(user, guild, waiter, client, rulesChannel);
                            }),
                            fail -> {
                            });
                }),
                fail -> {
                });
    }
}


