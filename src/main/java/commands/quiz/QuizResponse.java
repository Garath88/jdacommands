package commands.quiz;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern CORRECT_ANSWER_PATTERN = Pattern.compile("^((?i)asagi[\\s.,!]*?(igawa[.!\\s]*?)?|igawa[,]?\\sasagi[!.\\s]*?)$");

    QuizResponse(CommandClient client) {
        this.client = client;
        retryMessage = String.format(
            "- You can try again by typing **%smember**",
            client.getPrefix());
    }

    @Override
    public void apply(Guild guild, MessageReceivedEvent e, EventWaiter waiter) {
        User user = e.getAuthor();
        String response = e.getMessage().getContentRaw().toLowerCase();
        checkResponse(response, guild, user, waiter);
    }

    void checkResponse(String response, Guild guild, User user, EventWaiter waiter) {
        JDA jda = guild.getJDA();
        Matcher matcher = CORRECT_ANSWER_PATTERN.matcher(response);
        if (matcher.find()) {
            MessageUtil.sendMessageToUser(user, EmojiUtil.getCustomEmoji(jda, "sakura"));
            MessageUtil.sendMessageToUser(user, "- Correct");
            RoleUtil.addRole(guild, user, QuizQuestion.RULES_ROLE);
            RoleUtil.removeRole(guild, user, QuizQuestion.QUIZ_ROLE);
            RulesMessage.perform(user, guild, waiter, client);
        } else if ("sakura".equals(response) || "sakura igawa".equals(response) || "igawa sakura".equals(response)) {
            MessageUtil.sendMessageToUser(user,
                "- Mee?! Why would I fight myself? You silly goose! :smile:\n"
                    + retryMessage);
        } else {
            MessageUtil.sendMessageToUser(user,
                String.format("- Aww.. wrong answer %s \n"
                        + retryMessage,
                    EmojiUtil.getCustomEmoji(jda, "feelsbadman")));
            MessageUtil.sendMessageToUser(guild.getOwner().getUser(),
                String.format("%s#%s: %s%n%s",
                    user.getName(), user.getDiscriminator(), response, user.getAsMention()));
        }
    }

    String getRetryMessage() {
        return retryMessage;
    }
}
