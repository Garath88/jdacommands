package utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public final class GuildUtil {
    private GuildUtil() {
    }

    public static Guild getGuild(JDA jda) {
        return jda.getGuilds().stream()
            .findFirst()
            .orElseThrow(IllegalStateException::new);
    }
}
