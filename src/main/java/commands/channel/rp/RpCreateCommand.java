package commands.channel.rp;

import java.util.Collections;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.channel.ChannelInfo;
import commands.channel.database.RpChannelsDbTable;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import utils.CategoryUtil;
import utils.GuildUtil;
import utils.TextChannelUtil;

public class RpCreateCommand extends Command {
    private final EventWaiter waiter;

    public RpCreateCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "rp_create";
        this.help = "Creates a new RP channel with description.";
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
        this.requiredRoles = Collections.singletonList("Roleplayer");
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        TextChannelUtil.addNewThread(event, waiter, RpCreateCommand::createThreadChannel);
    }

    private static void createThreadChannel(CommandEvent event, ChannelInfo channelInfo) {
        net.dv8tion.jda.api.entities.Category rpCategory = CategoryUtil.getRpCategory(event.getJDA());
        String name = channelInfo.getName();
        String description = channelInfo.getDescription();
        final String channelTopic = name.replaceAll(" ", "` `");
        GuildUtil.getGuild(event.getJDA()).createTextChannel(channelTopic)
            .setTopic(description)
            .setNSFW(true)
            .setParent(rpCategory)
            .queue(channel -> doTasks(channel, event));
    }

    private static void doTasks(TextChannel channel, CommandEvent event) {
        TextChannelUtil.blockQuizUsersForChannel(channel, event);
        TextChannel textChannel = TextChannelUtil.getChannel(channel.getId(), event.getJDA());
        RpChannelsDbTable.addChannel(event.getAuthor(), textChannel);
        event.reply(String.format("Successfully created new thread: **%s**", textChannel.getAsMention()));
    }
}
