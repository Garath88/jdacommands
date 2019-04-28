package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ISnowflake;

public final class EmojiUtil {
    private static final Pattern EMOJI_PATTERN = Pattern.compile(":([A-z]*?):");

    private EmojiUtil() {
    }

    public static String addEmojisToMessage(JDA jda, String message) {
        Matcher m = EMOJI_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer(message.length());
        while (m.find()) {
            String emoji = getCustomEmoji(jda, m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(emoji));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String getCustomEmoji(JDA jda, String emojiName) {
        String emojiId = jda.getEmotesByName(emojiName, false).stream()
            .findFirst()
            .map(ISnowflake::getId)
            .orElse(null);
        if (emojiId != null) {
            return String.format("<:%s:%s>", emojiName, emojiId);
        }
        return "";
    }
}
