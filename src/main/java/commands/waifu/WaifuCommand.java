package commands.waifu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import commands.waifu.promt.WaifuQuestion;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import utils.ArgumentChecker;
import utils.GuildUtil;
import utils.MessageCycler;
import utils.RoleUtil;

public class WaifuCommand extends Command {
    private static final String OBORO = "Oboro";
    private static final String SAKURA = "Sakura";
    private static final int MAX_CHAR_LENGTH = 32;
    private Roles roles;
    private final EventWaiter waiter;
    private static final MessageCycler selfMessageCycler =
        new MessageCycler(Arrays.asList(
            "- Yippeee!!!!!",
            ":heart:",
            "- YAY!",
            "- MEEE? Awww :blush:",
            "- Woohoo!",
            "- 11 points to Sakura!",
            "- Thank you! :heart:",
            ":kissing_heart:",
            "- Thank you for supporting me! <3",
            "- Thankies~"
        ), 0, true);
    private static final MessageCycler otherWaifusMessageCycler =
        new MessageCycler(Arrays.asList(
            "- Excellent choice!",
            "- She is soo cute!",
            "<3",
            "- I wanna be just like her!",
            "- She is so cool!",
            "- Good choice!",
            "- Isn't she pretty?",
            "- Here ya go!",
            "- You are all set!"
        ), 0, true);
    private static final MessageCycler zaidanMessageCycler =
        new MessageCycler(Arrays.asList(
            "- She is amazing! Wait..",
            "- It's pronounced Zai-Den not Zai-Dan *duh*",
            "- Are you sure you know what a Waifu is?",
            "- Oke..",
            "- Now you are just messing with me",
            "- Fine, I give up..",
            "- All aboard the Zaidan train!",
            "- Make sure to give him that like and subscribe!",
            "- GO GO GO Zaidan!"
        ), 0, false);

    public WaifuCommand(EventWaiter waiter) {
        this.name = "waifu";
        this.help = "Represent your favorite waifu by showing off your role and joining their team! ";
        this.arguments = "<name> or <none>";
        this.guildOnly = false;
        this.roles = new WaifuRoles();
        this.waiter = waiter;
        this.botPermissions = new Permission[] {
            Permission.NICKNAME_MANAGE
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            if (event.getArgs().isEmpty()) {
                WaifuQuestion.perform(event, waiter, roles);
            } else {
                checkArgument(event, event.getArgs(), roles);
            }
        } catch (IllegalArgumentException | InsufficientPermissionException | HierarchyException e) {
            event.replyWarning(String.format("%s %s",
                e.getMessage(), event.getMessage().getAuthor().getAsMention()));
        }
    }

    public static void checkArgument(CommandEvent event, String waifu, Roles roles) {
        if (waifu.equalsIgnoreCase("none")) {
            removeWaifuRoles(roles, event);
        } else {
            addWaifuRole(event, waifu, roles);
        }
    }

    private static void addWaifuRole(CommandEvent event, String input, Roles roles) {
        Guild guild = GuildUtil.getGuild(event.getJDA());
        Member member = guild.getMember(event.getAuthor());
        if (member != null) {
            String waifu = validateAndFindWaifuRole(input, roles, guild.getJDA());
            addDefaultWaifuSupportRole(guild, member);
            removePreviousWaifuRole(guild, member, roles);
            Role guildRole = RoleUtil.findRole(guild, roles.getRole(waifu));

            doResponses(waifu, input, event);
            guild.addRoleToMember(member, guildRole)
                .queue(success -> changeUserNicknameBasedOnRole(guildRole, member, roles, event));
        }
    }

    private static void removeWaifuRoles(Roles roles, CommandEvent event) {
        if (event.getSelfUser().getName().equals(OBORO)) {
            event.reply("- Nonono I insist..");
            addWaifuRole(event, OBORO, roles);
        } else {
            Guild guild = GuildUtil.getGuild(event.getJDA());
            List<String> waifuRoles = roles.getRoles();
            waifuRoles.add("Waifu Supporter");
            Collection<Role> rolesToBeRemoved = RoleUtil.getMemberRoles(event)
                .stream()
                .filter(role -> waifuRoles.contains(role.getName()))
                .collect(Collectors.toList());
            Member member = guild.getMember(event.getAuthor());
            if (member != null) {
                List<Role> memberRoles = member.getRoles();
                memberRoles.removeAll(rolesToBeRemoved);
                guild.modifyMemberRoles(member, memberRoles)
                    .queue(success -> {
                        String currentNickname = member.getNickname();
                        User user = member.getUser();
                        if (StringUtils.isEmpty(currentNickname)) {
                            currentNickname = user.getName();
                        }
                        String newNickname = getNicknameWithoutTeamname(currentNickname, roles);
                        member.modifyNickname(newNickname).queue(success2 -> {
                            if (rolesToBeRemoved.isEmpty()) {
                                event.reply(String.format("You have no waifu %s",
                                    user.getAsMention()));
                            } else {
                                event.reply(String.format("Successfully removed the roles: %s for %s",
                                    getRoleNames(rolesToBeRemoved, event),
                                    user.getAsMention()));
                            }
                        });
                    });
            }
        }
    }

    private static void addDefaultWaifuSupportRole(Guild guild, Member member) {
        Role waifuSupporterRole = RoleUtil.findRole(guild, "Waifu Supporter");
        guild.addRoleToMember(member, waifuSupporterRole)
            .queue();
    }

    private static String validateAndFindWaifuRole(String waifu, Roles roles, JDA jda) {
        ArgumentChecker.checkArgsBySpaceIsAtMax(waifu, 2);

        if (jda.getSelfUser().getName().equals(SAKURA)) {
            if (waifu.toLowerCase().contains("oboro")) {
                throw new IllegalArgumentException(
                    "Oboro?! Are you really sure?\n"
                        + "- Then I think you should try asking her with **-waifu**");
            } else {
                Optional<String> waifuRole = findBestMatchingRole(waifu, roles);
                if (!waifuRole.isPresent()) {
                    throw new IllegalArgumentException(String.format("I can't add \"%s\" sorry!",
                        waifu));
                }
                return waifuRole.get();
            }
        } else {
            return OBORO;
        }
    }

    private static Optional<String> findBestMatchingRole(String waifu, Roles roles) {
        String temp = waifu;
        temp = temp.toLowerCase();
        return roles.getRoleArguments().stream()
            .map(String::toLowerCase)
            .filter(temp::contains)
            .findFirst();
    }

    private static void removePreviousWaifuRole(Guild guild, Member member, Roles roles) {
        List<String> userRoles = member.getRoles().stream().map(Role::getName)
            .collect(Collectors.toList());
        for (String waifuRole : getPreviousWaifuRole(userRoles, roles)) {
            RoleUtil.removeRole(guild, member.getUser(), waifuRole);
        }
    }

    private static List<String> getPreviousWaifuRole(List<String> userRoles, Roles roles) {
        return userRoles.stream()
            .filter(role -> roles.getRoles().contains(role))
            .collect(Collectors.toList());
    }

    private static void changeUserNicknameBasedOnRole(Role guildRole, Member member, Roles roles, CommandEvent event) {
        String role = guildRole.getName();
        String currentNickname = member.getNickname();
        if (StringUtils.isEmpty(currentNickname)) {
            currentNickname = member.getUser().getName();
        }
        currentNickname = getNicknameWithoutTeamname(currentNickname, roles);
        String newNickname = String.format("%s \"%s\"", currentNickname, roles.getRoleRepresentation(role));
        if (newNickname.length() > MAX_CHAR_LENGTH) {
            int numberOfCharactersToBeRemoved = newNickname.length() - MAX_CHAR_LENGTH;
            newNickname = String.format("%s \"%s\"",
                currentNickname.substring(0, currentNickname.length() - numberOfCharactersToBeRemoved), roles.getRoleRepresentation(role));
            if (event.getSelfUser().getName().equals(SAKURA)) {
                event.reply("- Your nickname was a bit too long so I had to shorten it.. Sorry!");
            } else {
                event.reply("- What kind of idiot uses such a long and ridiculous name?");
            }
        }

        if (!newNickname.equals(currentNickname)) {
            String roleName = event.isFromType(ChannelType.PRIVATE) ? guildRole.getName() : guildRole.getAsMention();
            GuildUtil.getGuild(event.getJDA()).modifyNickname(member, newNickname)
                .queue(success -> event.reply(String.format("%s You now have the role %s!",
                    event.getAuthor().getAsMention(), roleName)));

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

    private static String getRoleNames(Collection<Role> rolesToBeRemoved, CommandEvent event) {
        Stream<String> roleNames;
        if (event.isFromType(ChannelType.PRIVATE)) {
            roleNames = rolesToBeRemoved.stream()
                .map(Role::getName);
        } else {
            roleNames = rolesToBeRemoved.stream()
                .map(Role::getAsMention);
        }
        return roleNames.collect(Collectors.joining(", "));
    }

    private static void doResponses(String waifu, String input, CommandEvent event) {
        if (event.getSelfUser().getName().equals(SAKURA)) {
            if (waifu.equals("sakura")) {
                event.reply(selfMessageCycler.getNextMessageAfterTimer());
            } else if (waifu.equals("zaidan")) {
                event.reply(zaidanMessageCycler.getNextMessageAfterTimer());
            } else {
                event.reply(otherWaifusMessageCycler.getNextMessageAfterTimer());
            }
        } else {
            if (!input.toLowerCase().contains("oboro") && !input.toLowerCase().contains("waifu")) {
                event.reply("- That pig? Surely you misspoke, but don't worry I wont punish you for your incompetence...");
            }
        }
    }
}
