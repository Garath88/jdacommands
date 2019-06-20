package commands.waifu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.waifu.promt.WaifuQuestion;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import utils.ArgumentChecker;
import utils.GuildUtil;
import utils.RoleUtil;

public class WaifuCommand extends Command {
    private static final int MAX_CHAR_LENGTH = 32;
    private Roles roles;
    private final EventWaiter waiter;

    public WaifuCommand(Roles roles, EventWaiter waiter) {
        this.name = "waifu";
        this.help = "Represent your favorite waifu by showing off your role and joining their team! ";
        this.arguments = "<name> or <none>";
        this.guildOnly = false;
        this.roles = roles;
        this.waiter = waiter;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            if (event.getArgs().isEmpty()) {
                WaifuQuestion.perform(event, waiter, roles);
            } else {
                addWaifuRole(event, event.getArgs(), roles);
            }
        } catch (IllegalArgumentException | InsufficientPermissionException | HierarchyException e) {
            event.replyWarning(String.format("%s %s",
                e.getMessage(), event.getMessage().getAuthor().getAsMention()));
        }
    }

    public static void addWaifuRole(CommandEvent event, String waifu, Roles roles) {
        if (waifu.equals("none")) {
            removeWaifuRoles(event, roles);
        } else {
            addDefaultWaifuSupportRole(event.getGuild(), event.getMember());
            waifu = validateAndFindWaifuRole(waifu, roles);
            removePreviousWaifuRole(event, roles);
            Guild guild = event.getGuild();
            Role guildRole = RoleUtil.findRole(guild, roles.getRole(waifu));
            guild.getController().addSingleRoleToMember(event.getMember(), guildRole)
                .complete();
            changeUserNicknameBasedOnRole(guildRole, event, roles);
        }
    }

    private static void removeWaifuRoles(CommandEvent event, Roles roles) {
        List<String> waifuRoles = roles.getRoles();
        waifuRoles.add("Waifu Supporter");
        Collection<Role> rolesToBeRemoved = RoleUtil.getMemberRoles(event)
            .stream()
            .filter(role -> waifuRoles.contains(role.getName()))
            .collect(Collectors.toList());
        Guild guild = event.getGuild();
        guild.getController().removeRolesFromMember(event.getMember(), rolesToBeRemoved)
            .queue(success -> {
                String roleNames = rolesToBeRemoved.stream()
                    .map(Role::getAsMention)
                    .collect(Collectors.joining(", "));
                String currentNickname = event.getMember().getNickname();
                if (StringUtils.isEmpty(currentNickname)) {
                    currentNickname = event.getAuthor().getName();
                }
                String newNickname = getNicknameWithoutTeamname(currentNickname, roles);
                guild.getController().setNickname(event.getMember(), newNickname)
                    .queue(success2 -> event.reply(String.format("Successfully removed the roles: %s for %s",
                        roleNames, event.getAuthor().getAsMention())));
            });
    }

    private static void addDefaultWaifuSupportRole(Guild guild, Member member) {
        Role waifuSupporterRole = RoleUtil.findRole(guild, "Waifu Supporter");
        guild.getController().addSingleRoleToMember(member, waifuSupporterRole)
            .queue();
    }

    private static String validateAndFindWaifuRole(String waifu, Roles roles) {
        ArgumentChecker.checkArgsBySpaceIsAtMax(waifu, 2);
        Optional<String> waifuRole = findBestMatchingRole(waifu, roles);
        if (!waifuRole.isPresent()) {
            throw new IllegalArgumentException(String.format("- Umm.. Who is \"%s\" again?",
                waifu));
        }
        return waifuRole.get();
    }

    private static Optional<String> findBestMatchingRole(String waifu, Roles roles) {
        String temp = waifu;
        temp = temp.toLowerCase();
        return roles.getRoleArguments().stream()
            .map(String::toLowerCase)
            .filter(temp::contains)
            .findFirst();
    }

    private static void removePreviousWaifuRole(CommandEvent event, Roles roles) {
        List<String> userRoles = event.getMember().getRoles().stream().map(Role::getName)
            .collect(Collectors.toList());
        for (String waifuRole : getPreviousWaifuRole(userRoles, roles)) {
            RoleUtil.removeRole(event.getGuild(), event.getAuthor(), waifuRole);
        }
    }

    private static List<String> getPreviousWaifuRole(List<String> userRoles, Roles roles) {
        return userRoles.stream()
            .filter(role -> roles.getRoles().contains(role))
            .collect(Collectors.toList());
    }

    private static void changeUserNicknameBasedOnRole(Role guildRole, CommandEvent event, Roles roles) {
        String role = guildRole.getName();
        String currentNickname = event.getMember().getNickname();
        if (StringUtils.isEmpty(currentNickname)) {
            currentNickname = event.getAuthor().getName();
        }
        currentNickname = getNicknameWithoutTeamname(currentNickname, roles);
        String newNickname = String.format("%s \"%s\"", currentNickname, roles.getRoleRepresentation(role));
        if (newNickname.length() > MAX_CHAR_LENGTH) {
            int numberOfCharactersToBeRemoved = newNickname.length() - MAX_CHAR_LENGTH;
            newNickname = String.format("%s \"%s\"",
                currentNickname.substring(0, currentNickname.length() - numberOfCharactersToBeRemoved), roles.getRoleRepresentation(role));
            event.reply("- Your nickname was a bit too long so I had to shorten it.. Sorry!");
        }

        if (!newNickname.equals(currentNickname)) {
            GuildUtil.getGuild(event.getJDA()).getController().setNickname(event.getMember(), newNickname)
                .queue(success -> event.reply(String.format("%s You now have the role %s!",
                    event.getAuthor().getAsMention(), guildRole.getAsMention())));

        }
    }

    private static String getNicknameWithoutTeamname(String currentNickname, Roles roles) {
        List<String> allTeamNames = new ArrayList<>();
        roles.getRoles().forEach(role -> allTeamNames.add(roles.getRoleRepresentation(role)));
        for (String teamName : allTeamNames) {
            currentNickname = currentNickname.replaceAll(String.format("\"%s\"",
                teamName), "");
        }
        currentNickname = currentNickname.trim();
        return currentNickname;
    }
}