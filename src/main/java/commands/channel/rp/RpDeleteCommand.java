package commands.channel.rp;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.channel.database.ChannelDbInfo;
import commands.channel.database.RpChannelsDbTable;
import commands.channel.thread.DeleteThreadCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import utils.CategoryUtil;
import utils.GuildUtil;

public class RpDeleteCommand extends Command {

    public RpDeleteCommand() {
        this.name = "rp_delete";
        this.help = "Choose a created RP channel to delete.";
        this.guildOnly = false;
        this.botPermissions = new Permission[] {
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        JDA jda = event.getJDA();
        Member member = GuildUtil.getGuild(jda).getMember(event.getAuthor());
        if (member != null) {
            ChannelDbInfo userThreadInfo = RpChannelsDbTable.getChannelInfoFromUser(member.getUser());
            DeleteThreadCommand.showCreatedChannelsToDelete(event, userThreadInfo, CategoryUtil.getRpCategory(jda));
        }
    }
}
