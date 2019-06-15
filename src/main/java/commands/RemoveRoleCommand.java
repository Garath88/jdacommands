package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import utils.ArgumentChecker;
import utils.RoleUtil;

public class RemoveRoleCommand extends Command {

    public RemoveRoleCommand() {
        this.name = "remove_role";
        this.help = "Removes a given role for all members.";
        this.arguments = "<role name>";
        this.guildOnly = true;
        this.ownerCommand = true;
        this.botPermissions = new Permission[] {
            Permission.MANAGE_ROLES
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String arguments = event.getArgs();
            ArgumentChecker.checkIfArgsAreNotEmpty(arguments);
            Guild guild = event.getGuild();
            Role roleToBeRemoved = RoleUtil.findRole(guild, arguments);
            guild.getMembersWithRoles(roleToBeRemoved)
                .forEach(member -> RoleUtil.removeRole(guild, member.getUser(), roleToBeRemoved.getName()));
            event.replySuccess(String.format("Successfully removed %s for all members",
                roleToBeRemoved.getAsMention()));
        } catch (IllegalArgumentException | HierarchyException e) {
            event.replyWarning(e.getMessage());
        }
    }
}