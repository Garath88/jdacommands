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
    private static final int REPLY_DELAY = 1000;
    private CommandClient client;
    private final String retryMessage;

    QuizResponse(CommandClient client) {
        this.client = client;
        retryMessage = String.format(
            "- You can try again by typing **%smember**",
            client.getPrefix());
    }

    @Override
    public void apply(Guild guild, MessageReceivedEvent event, EventWaiter waiter) {
        String response = event.getMessage().getContentRaw();
        checkResponse(response, guild, event, waiter);
    }

    void checkResponse(final String response, Guild guild, MessageReceivedEvent event, EventWaiter waiter) {
        User user = event.getAuthor();
        String answer = response.toLowerCase().replace(".", "");
        JDA jda = guild.getJDA();
        if (answer.equals("kyousuke") ||
            answer.equals("kyosuke") ||
            answer.equals("kyousuke sawaki") ||
            answer.equals("sawaki kyousuke") ||
            answer.equals("kyosuke sawaki") ||
            answer.equals("sawaki kyosuke")
        ) {
            MessageUtil.sendMessageToUser(user, EmojiUtil.getCustomEmoji(jda, "sakura"));
            MessageUtil.sendMessageToUser(user, "- Correct", REPLY_DELAY);
            RoleUtil.addRole(guild, user, QuizQuestion.RULES_ROLE);
            RoleUtil.removeRole(guild, user, QuizQuestion.QUIZ_ROLE);
            HelpMessage.perform(user, guild, waiter, client, event);
        } else if (answer.equals("sawaki")) {
            MessageUtil.sendMessageToUser(user, "- Sawaki? Sawaki who?? \n"
                + retryMessage, REPLY_DELAY);
        } else if ("sakura".equals(answer) || "sakura igawa".equals(answer) || "igawa sakura".equals(answer)) {
            MessageUtil.sendMessageToUser(user,
                "- Yes? Wait.. Meee?!! You are such a silly goose! :smile:\n"
                    + retryMessage, REPLY_DELAY);
        } else if (answer.contains("?")) {
            MessageUtil.sendMessageToUser(user,
                "- It's rude to answer a question with a question! \n"
                    + retryMessage, REPLY_DELAY);
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