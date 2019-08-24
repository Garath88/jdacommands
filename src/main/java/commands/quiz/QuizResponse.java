package commands.quiz;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import utils.EmojiUtil;
import utils.MessageUtil;
import utils.RoleUtil;
import utils.TriFunction;

public class QuizResponse implements TriFunction<Guild, MessageReceivedEvent, EventWaiter> {
    private CommandClient client;
    private final String retryMessage;

    QuizResponse(CommandClient client) {
        this.client = client;
        retryMessage = String.format(
            "- You can try again by typing **%smember**",
            client.getPrefix());
    }

    @Override
    public void apply(Guild guild, MessageReceivedEvent e, EventWaiter waiter) {
        User user = e.getAuthor();
        String response = e.getMessage().getContentRaw();
        checkResponse(response, guild, user, waiter);
    }

    void checkResponse(final String response, Guild guild, User user, EventWaiter waiter) {
        String answer = response;
        answer = answer.replace(".", "");
        JDA jda = guild.getJDA();
        if (answer.equalsIgnoreCase("kyousuke") ||
            answer.equalsIgnoreCase("kyosuke") ||
            answer.equalsIgnoreCase("kyousuke sawaki") ||
            answer.equalsIgnoreCase("sawaki kyousuke") ||
            answer.equalsIgnoreCase("kyosuke sawaki") ||
            answer.equalsIgnoreCase("sawaki kyosuke")
        ) {
            MessageUtil.sendMessageToUser(user, EmojiUtil.getCustomEmoji(jda, "sakura"));
            MessageUtil.sendMessageToUser(user, "- Correct");
            RoleUtil.addRole(guild, user, QuizQuestion.RULES_ROLE);
            RoleUtil.removeRole(guild, user, QuizQuestion.QUIZ_ROLE);
            RulesMessage.perform(user, guild, waiter, client);
        } else if (answer.equalsIgnoreCase("sawaki")) {
            MessageUtil.sendMessageToUser(user, "Sawaki? Sawaki who?? \n"
                + retryMessage);
        } else if ("sakura".equals(answer) || "sakura igawa".equals(answer) || "igawa sakura".equals(answer)) {
            MessageUtil.sendMessageToUser(user,
                "- Yes? Wait.. Meee?!! You are such a silly goose! :smile:\n"
                    + retryMessage);
        } else if (answer.contains("?")) {
            MessageUtil.sendMessageToUser(user,
                "- It's rude to  answer a question with a question! \n"
                    + retryMessage);
            MessageUtil.sendMessageToUser(guild.getOwner().getUser(),
                String.format("%s: %s%n%s",
                    user.getAsTag(), response, user.getAsMention()));
        } else {
            MessageUtil.sendMessageToUser(user,
                String.format("- Aww.. wrong answer %s \n"
                        + retryMessage,
                    EmojiUtil.getCustomEmoji(jda, "feelsbadman")));
            MessageUtil.sendMessageToUser(guild.getOwner().getUser(),
                String.format("%s: %s%n%s",
                    user.getAsTag(), response, user.getAsMention()));
        }
    }

    String getRetryMessage() {
        return retryMessage;
    }
}
