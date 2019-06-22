package utils;

import java.util.List;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public final class RoleUtil {
    private RoleUtil() {
    }

    public static void addRole(Guild guild, User user, String roleName) {
        Role role = findRole(guild, roleName);
        addRole(guild, user, role);
    }

    private static void addRole(Guild guild, User user, Role role) {
        Member member = FinderUtil.findMembers(user.getId(), guild)
            .stream()
            .findFirst()
            .orElseThrow(IllegalStateException::new);
        guild.getController().addSingleRoleToMember(member, role)
            .queue(success -> {
            }, fail -> {
            });
    }

    public static Role findRole(Guild guild, String roleName) {
        if (guild == null) {
            throw new IllegalArgumentException("Guild not found!");
        }
        String temp = roleName;
        if (roleName.startsWith("<")) {
            temp = temp.replaceAll("[@&<#>]", "");
            return guild.getRoleById(temp);
        } else {
            return guild.getRolesByName(temp, false).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Role \"%s\" doesn't exist!", roleName)));
        }
    }

    public static void removeRole(Guild guild, User user, String roleName) {
        Role role = findRole(guild, roleName);
        Member member = FinderUtil.findMembers(user.getId(), guild)
            .stream()
            .findFirst()
            .orElseThrow(IllegalStateException::new);
        guild.getController().removeSingleRoleFromMember(member, role)
            .queue(success -> {
            }, fail -> {
            });
    }

    public static List<Role> getMemberRoles(CommandEvent event) {
        Guild guild = GuildUtil.getGuild(event.getJDA());
        Member member = FinderUtil.findMembers(event.getAuthor().getId(), guild)
            .stream()
            .findFirst()
            .orElseThrow(IllegalStateException::new);
        return member.getRoles();
    }
}
