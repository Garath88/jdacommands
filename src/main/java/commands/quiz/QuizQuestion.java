package commands.quiz;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import utils.CategoryUtil;
import utils.GuildUtil;
import utils.MessageUtil;
import utils.PrivateChannelWrapper;
import utils.RoleUtil;

public final class QuizQuestion {
    static final String QUIZ_QUESTION =
        "- **In TA 1 what was the name of the place where my sister was taken after being abducted by Oboro (VN and OVA)?**";
    public static final String QUIZ_ROLE = "Quiz";
    public static final String RULES_ROLE = "Rules";
    static final int QUIZ_TIMEOUT_IN_SECONDS = 10 * 60;

    private QuizQuestion() {
    }

    public static void perform(GenericEvent event, EventWaiter waiter, CommandClientImpl client) {
        Member member = ((GuildMemberJoinEvent)event).getMember();
        User user = member.getUser();
        if (!user.isBot()) {
            Guild guild = GuildUtil.getGuild(event.getJDA());
            RoleUtil.addRole(guild, user, QUIZ_ROLE);
            blockMemberFromSelfRoleChannels(guild, member);
            user.openPrivateChannel()
                .queue(PrivateChannelWrapper.userIsInGuild(pc ->
                    pc.sendMessage("*Yohoo~* it's Sakura! :heart:")
                        .queue(PrivateChannelWrapper.userIsInGuild(msg2 -> {
                                pc.sendTyping().queue();
                                pc.sendMessage("- In order to gain access to this lewd server you must first answer **one** simple **question!**")
                                    .queueAfter(3, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(msg3 -> {
                                        pc.sendTyping().queue();
                                        pc.sendMessage("- Ready? ")
                                            .queueAfter(3, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(msg4 ->
                                                msg4.editMessage("- Ready? Great, let's start!")
                                                    .queueAfter(1, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(msg5 -> {
                                                        pc.sendTyping().queue();
                                                        pc.sendMessage(QUIZ_QUESTION)
                                                            .queueAfter(3, TimeUnit.SECONDS,
                                                                PrivateChannelWrapper.userIsInGuild(listen -> {
                                                                    QuizResponse quizResponse = new QuizResponse(client);
                                                                    MessageUtil.waitForResponseInDM(user, guild, waiter,
                                                                        quizResponse, QuizQuestion.QUIZ_TIMEOUT_IN_SECONDS,
                                                                        quizResponse.getRetryMessage(), client);
                                                                }),
                                                                fail -> {
                                                                });
                                                    }), fail -> {
                                                    })), fail -> {
                                            });
                                    }), fail -> {
                                    });
                            }
                        ), fail -> {
                        })), fail -> {
                });
        }
    }

    private static void blockMemberFromSelfRoleChannels(Guild guild, Member member) {
        CategoryUtil.getSelfRoleCategories(guild)
            .forEach(category -> category.upsertPermissionOverride(member)
                .deny(Permission.MESSAGE_READ)
                .queue());

        FinderUtil.findTextChannels("日本語-translations", guild).stream()
            .filter(Objects::nonNull)
            .forEach(channel -> channel.upsertPermissionOverride(member)
                .deny(Permission.MESSAGE_READ)
                .queue());
    }
}