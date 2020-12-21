package commands.system;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import utils.GuildUtil;

public class PruneCommand extends Command {
    private static Set<String> usersToPrune = new HashSet<>();
    private static AtomicInteger channelCounter = new AtomicInteger();

    public PruneCommand() {
        this.name = "prune";
        this.help = "Prunes lurkers that haven't posted anything in over a year";
        this.ownerCommand = true;
        this.guildOnly = false;
        this.botPermissions = new Permission[] {
            Permission.MESSAGE_READ,
            Permission.MESSAGE_HISTORY
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        JDA jda = event.getJDA();
        Guild guild = GuildUtil.getGuild(jda);
        List<User> potentialLurkers = guild.getMembers().stream()
            .filter(this::isNonFanUser)
            .filter(this::hasBeenMemberOverAYear)
            .map(Member::getUser)
            .collect(Collectors.toList());
        usersToPrune = potentialLurkers.stream()
            .map(User::getName)
            .collect(Collectors.toSet());
        hasAnyReactionOrPost(potentialLurkers.iterator(), guild.getTextChannels());
    }

    private boolean isNonFanUser(Member member) {
        return !member.getUser().isBot() && member.getRoles().stream()
            .map(Role::getName)
            .noneMatch(role -> Permissions.FAN.getValues().contains(role));
    }

    private boolean hasBeenMemberOverAYear(Member member) {
        Duration duration = Duration.between(member.getTimeJoined().toInstant(), Instant.now());
        return duration.toDays() >= 365;
    }

    private static void hasAnyReactionOrPost(Iterator<User> userIterator, List<TextChannel> channels) {
        while (userIterator.hasNext()) {
            User user = userIterator.next();
            channels.forEach(channel ->
            {
                if (usersToPrune.contains(user.getName())) {
                    channel.getIterableHistory().takeAsync(999999).thenAccept(history -> {
                            if (userHasMessage(user, history)) {
                                usersToPrune.remove(user.getName());
                            }
                        }
                    );
                } else {
                    System.out.print("APA");
                }
            });
        }
    }

    private static boolean userHasMessage(User user, List<Message> messages) {
        if (messages.isEmpty()) {
            return false;
        }
        Iterator<Message> iter = messages.iterator();
        if (!messages.isEmpty()) {
            while (iter.hasNext()) {
                Message latestMessage = iter.next();
                if (latestMessage.getAuthor().equals(user)) {
                    return true;
                }
            }
        }
        return false;
    }
}

