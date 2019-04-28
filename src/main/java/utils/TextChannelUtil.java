package utils;

import java.util.List;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;

public final class TextChannelUtil {
    private TextChannelUtil() {
    }

    public static TextChannel getChannel(String channelId, Event event) {
        List<TextChannel> textChannels = event.getJDA().getTextChannels();
        return textChannels.stream().filter(textChannel -> textChannel.getId().equals(channelId))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(
                String.format("Could not find channel: %s", channelId)));
    }
}
