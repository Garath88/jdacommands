package commands.quiz;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import utils.GuildUtil;
import utils.MessageUtil;

public class MemberCommand extends Command {
    private final EventWaiter waiter;

    public MemberCommand(EventWaiter waiter) {
        this.name = "member";
        this.help = "Answer a question for getting the member role";
        this.guildOnly = false;
        this.waiter = waiter;
        this.hidden = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            Guild guild = GuildUtil.getGuild(event.getJDA());
            Member member = FinderUtil.findMembers(event.getAuthor().getId(), guild)
                .stream()
                .findFirst()
                .orElseThrow(IllegalStateException::new);
            if (!member.getRoles().isEmpty() && event.getChannelType().equals(ChannelType.PRIVATE)) {
                QuizResponse quizResponse = new QuizResponse(event.getClient());
                String response = event.getArgs();
                if (response.isEmpty()) {
                    event.reply(QuizQuestion.QUIZ_QUESTION);
                    MessageUtil.waitForResponseInDM(event.getAuthor(), guild, waiter,
                        new QuizResponse(event.getClient()), QuizQuestion.QUIZ_TIMEOUT_IN_MIN,
                        quizResponse.getRetryMessage());
                } else {
                    quizResponse.checkResponse(response, guild, event.getAuthor(), waiter);

                }
            }
        } catch (IllegalArgumentException e) {
            event.replyWarning(String.format("%s %s",
                event.getMessage().getAuthor().getAsMention(), e.getMessage()));
        }
    }
}
