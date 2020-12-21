package commands.quiz;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utils.CategoryUtil;
import utils.PrivateChannelWrapper;
import utils.RoleUtil;
import utils.TriFunction;

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
            unblockMemberFromSelfRoleChannels(guild, guild.getMember(user));
            user.openPrivateChannel().queue(PrivateChannelWrapper.userIsInGuild(pc -> {
                    pc.sendTyping().queue();
                    pc.sendMessage(
                        "- Awesome! Welcome!").queueAfter(500, TimeUnit.MILLISECONDS,
                        PrivateChannelWrapper.userIsInGuild(
                        success -> {
                            pc.sendTyping().queue();
                            pc.sendMessage(
                                "- P.S. You can react in server info to gain access to even more channels.")
                                .queueAfter(2000, TimeUnit.MILLISECONDS);
                        }));
                }),
                fail -> {
                });
        } else {
            RulesQuestion.perform(RulesMessage.TIME_TO_READ_RULES_IN_SEC + 5,
                user, guild, waiter, client);
        }
    }

    private static void unblockMemberFromSelfRoleChannels(Guild guild, Member member) {
        CategoryUtil.getSelfRoleCategories(guild)
            .forEach(category -> category.getPermissionOverride(member)
                .delete()
                .queue());
        FinderUtil.findTextChannels("日本語-translations", guild).stream()
            .filter(Objects::nonNull)
            .forEach(channel -> channel.getPermissionOverride(member)
                .delete()
                .queue());
    }
}