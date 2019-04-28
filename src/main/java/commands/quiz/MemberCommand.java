package commands.quiz;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import commands.quiz.QuizQuestion;
import commands.quiz.QuizResponse;

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
            validateInput(event.getArgs());
            Guild guild = GuildUtil.getGuild(event.getJDA());
            Member member = FinderUtil.findMembers(event.getAuthor().getId(), guild)
                .stream()
                .findFirst()
                .orElseThrow(IllegalStateException::new);
            if (!member.getRoles().isEmpty() && event.getChannelType().equals(ChannelType.PRIVATE)) {
                event.reply(QuizQuestion.QUIZ_QUESTION);
                QuizResponse quizResponse = new QuizResponse(event.getClient());
                MessageUtil.waitForResponseInDM(event.getAuthor(), guild, waiter,
                    new QuizResponse(event.getClient()), QuizQuestion.QUIZ_TIMEOUT_IN_MIN,
                    quizResponse.getRetryMessage());
            }
        } catch (IllegalArgumentException e) {
            event.replyWarning(String.format("%s %s",
                event.getMessage().getAuthor().getAsMention(), e.getMessage()));
        }
    }

    private void validateInput(String args) {
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Please only type **+member** without any arguments!");
        }
    }
}
