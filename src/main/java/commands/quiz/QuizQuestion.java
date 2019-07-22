package commands.quiz;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import utils.GuildUtil;
import utils.MessageUtil;
import utils.PrivateChannelWrapper;
import utils.RoleUtil;

public final class QuizQuestion {
    static final String QUIZ_QUESTION =
        "- **Who do I \"fight to the death\" in order to free myself and my sister in Taimanin Asagi 1?** *(As seen in OVA 1 episodes 1-4 or VN 1)*";
    public static final String QUIZ_ROLE = "Quiz";
    public static final String RULES_ROLE = "Rules";
    static final int QUIZ_TIMEOUT_IN_SECONDS = 10 * 60;

    private QuizQuestion() {
    }

    public static void perform(Event event, EventWaiter waiter, CommandClientImpl client) {
        User user = ((GuildMemberJoinEvent)event).getMember().getUser();
        if (!user.isBot()) {
            Guild guild = GuildUtil.getGuild(event.getJDA());
            RoleUtil.addRole(guild, user, QUIZ_ROLE);
            user.openPrivateChannel()
                .queue(PrivateChannelWrapper.userIsInGuild(pc ->
                    pc.sendMessage("*Yohoo~* it's Sakura! :heart:")
                        .queue(PrivateChannelWrapper.userIsInGuild(msg2 ->
                            {
                                pc.sendTyping().queue();
                                pc.sendMessage("- In order to gain access to this lewd server you must first answer **one** simple **question!**")
                                    .queueAfter(3, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(msg3 ->
                                    {
                                        pc.sendTyping().queue();
                                        pc.sendMessage("- Ready? ")
                                            .queueAfter(3, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(msg4 ->
                                                msg4.editMessage("- Ready? Great, let's start!")
                                                    .queueAfter(1, TimeUnit.SECONDS, PrivateChannelWrapper.userIsInGuild(msg5 ->
                                                    {
                                                        pc.sendTyping().queue();
                                                        pc.sendMessage(QUIZ_QUESTION)
                                                            .queueAfter(3, TimeUnit.SECONDS,
                                                                listen -> {
                                                                    QuizResponse quizResponse = new QuizResponse(client);
                                                                    MessageUtil.waitForResponseInDM(user, guild, waiter,
                                                                        quizResponse, QuizQuestion.QUIZ_TIMEOUT_IN_SECONDS,
                                                                        quizResponse.getRetryMessage(), client);
                                                                },
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
}