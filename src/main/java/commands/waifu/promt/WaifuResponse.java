package commands.waifu.promt;

import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.waifu.Roles;
import commands.waifu.WaifuCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class WaifuResponse implements Consumer<MessageReceivedEvent> {
    private CommandEvent event;
    private final EventWaiter waiter;
    private final Roles roles;

    WaifuResponse(CommandEvent event, EventWaiter waiter, Roles roles) {
        this.event = event;
        this.waiter = waiter;
        this.roles = roles;
    }

    @Override
    public void accept(MessageReceivedEvent e) {
        String response = e.getMessage()
            .getContentRaw();
        try {
            WaifuCommand.checkArgument(event, response, roles);
        } catch (IllegalArgumentException | InsufficientPermissionException | HierarchyException ex) {
            event.replyWarning(String.format("%s %s",
                ex.getMessage(), event.getMessage().getAuthor().getAsMention()));
        } catch (IllegalStateException ex) {
            event.replyWarning(String.format("Could not find waifu role: \"%s\"",
                response));
        }
    }
}
