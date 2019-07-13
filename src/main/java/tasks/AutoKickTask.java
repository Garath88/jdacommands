package tasks;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import utils.GuildUtil;
import utils.MessageUtil;
import utils.RoleUtil;

public class AutoKickTask extends Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoKickTask.class);
    private static final String WELCOME_CHANNEL_ID = "421959386068680714";
    private static final long MAX_IDLE_DAYS = 3;
    private static final String MESSAGE =
        "- You think you can just lurk at the gates of Lord Black’s fine establishment? You think someone will just let you in if you linger long enough? \n"
            + "- Don’t be a fool! You are nothing! A nobody! I can’t believe that cunt Sakura is at it again… Thinking she can just let people in if they answer her stupid little questions.\n"
            + "- I will enjoy punishing her severely the next time I see her! \n"
            + "*Oboro’s finger caresses her tight, wet slit as she loses herself, entertaining such a marvelous thought.*\n"
            + "- You will soon realize that I am not as nice as her! Now remove yourself, worm! Hahahaha!\n"
            + "**(You are being kicked for being inactive in the #welcome channel)**";
    private static final String IMAGE_URL = "https://i.postimg.cc/W3dV00Y7/angry.png";
    private static final MessageEmbed EMBED = new EmbedBuilder()
        .setColor(new Color(54, 57, 63))
        .setImage(IMAGE_URL)
        .build();
    private JDA jda;

    public AutoKickTask(long loopTimeInMinutes, long delayInMinutes, JDA jda) {
        super(loopTimeInMinutes, delayInMinutes);
        this.jda = jda;
    }

    @Override
    public void execute() {
        jda.getTextChannels().stream()
            .filter(chan -> chan.getId().equals(WELCOME_CHANNEL_ID))
            .findFirst()
            .ifPresent(chan -> {
                Guild guild = GuildUtil.getGuild(jda);
                Role quizRole = RoleUtil.findRole(guild, "Quiz");
                chan.getMembers().forEach(member -> {
                    if (member.getRoles().contains(quizRole) && member.getRoles().size() == 1) {
                        Duration duration = Duration.between(member.getJoinDate().toInstant(), Instant.now());
                        if (duration.toDays() >= MAX_IDLE_DAYS) {
                            LOGGER.debug(String.format("Kicking member %s",
                                member.getUser().getAsTag()));
                            MessageUtil.sendMessageToUser(guild.getOwner().getUser(), String.format("Kicking member %s",
                                member.getUser().getAsTag()));
                    /*
                    member.getUser().openPrivateChannel().queue(
                        pc -> pc.sendMessage(EMBED).queue(fileSent -> pc.sendMessage(MESSAGE).queue(
                            messageSent -> guild.getController().kick(member)
                                .queueAfter(10, TimeUnit.SECONDS),
                            fail -> {
                            }),
                            fail -> {
                            }),
                        fail -> {
                        });
                     */
                        }
                    }
                });
            });
    }
}