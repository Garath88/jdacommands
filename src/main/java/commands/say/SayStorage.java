package commands.say;

import java.util.Optional;

import com.jagrosh.jdautilities.command.CommandEvent;

public final class SayStorage {
    private static boolean useDash = true;
    private static String id = null;

    private SayStorage() {
    }

    public static Optional<String> getChannel() {
        return Optional.ofNullable(id);
    }

    static void setChannel(String id) {
        SayStorage.id = id;
    }

    static void toggleUseDash(CommandEvent event) {
        useDash = !useDash;
        if (useDash) {
            event.reply("I'm now using '-' when talking");
        } else {
            event.reply("I'm NOT using '-' when talking");
        }
    }

    static boolean getUseDash() {
        return useDash;
    }
}
