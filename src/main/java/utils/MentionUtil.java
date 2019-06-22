package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;

final class MentionUtil {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@[A-z]*#(\\d{4})");

    private MentionUtil() {
    }

    static String addMentionsToMessage(String message, JDA jda) {
        Matcher m = MENTION_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer(message.length());
        while (m.find()) {
            String mentionedMember = m.group(0).replaceAll("@", "");
            Guild guild = GuildUtil.getGuild(jda);
            FinderUtil.findMembers(mentionedMember, guild).stream()
                .findFirst()
                .ifPresent(member -> m.appendReplacement(sb,
                    Matcher.quoteReplacement(member.getAsMention())));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
