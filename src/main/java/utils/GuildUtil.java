package utils;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;

public final class GuildUtil {
    private GuildUtil() {
    }

    public static Guild getGuild(JDA jda) {
        return jda.getGuilds().stream()
            .findFirst()
            .orElseThrow(IllegalStateException::new);
    }
}
