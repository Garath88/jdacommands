package utils;

import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

public final class TextChannelUtil {
    private TextChannelUtil() {
    }

    public static TextChannel getChannel(String channelId, JDA jda) {
        List<TextChannel> textChannels = jda.getTextChannels();
        return textChannels.stream().filter(textChannel -> textChannel.getId().equals(channelId))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(
                String.format("Could not find channel: %s", channelId)));
    }
}
