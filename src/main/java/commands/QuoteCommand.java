package commands;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.copy.MediaPatterns;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeUtil;
import utils.ArgumentChecker;
import utils.GuildUtil;

public final class QuoteCommand extends Command {
    private Random random = new Random();

    public QuoteCommand() {
        this.name = "quote";
        this.help = "Quote a message specified with the id of the message.";
        this.arguments = "<message id>";
        this.botPermissions = new Permission[] {
            Permission.MESSAGE_READ
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String args = event.getArgs();
            ArgumentChecker.checkArgsBySpaceRequires(args, 1);
            GuildUtil.getGuild(event.getJDA()).getTextChannels().forEach(chan -> {
                if (chan.canTalk()) {
                    chan.retrieveMessageById(args).queue(message -> {
                        if (!message.getContentDisplay().isEmpty() || !message.getAttachments().isEmpty()) {
                            event.reply(String.format("*%s quoted:*",
                                event.getAuthor().getAsTag()),
                                createEmbed(message));
                            event.getMessage().delete().queue();
                        }
                    }, fail -> {
                    });
                }
            });
        } catch (IllegalArgumentException e) {
            event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
            event.getChannel().sendMessage(e.getMessage())
                .queue(success -> success.delete()
                    .queueAfter(10, TimeUnit.SECONDS));
        }
    }

    private MessageEmbed createEmbed(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        createHeader(builder, message);
        builder.setDescription(message.getContentDisplay());
        createFooter(builder, message);
        addImage(builder, message);
        builder.setColor(getRandomColor());
        return builder.build();
    }

    private void createHeader(EmbedBuilder builder, Message message) {
        User author = message.getAuthor();
        String messageId = String.format("https://dummyimage.com/600x400/000/fff&text=%s",
            message.getId());
        builder.setAuthor(author.getAsTag(), messageId, author.getAvatarUrl());
    }

    private void createFooter(EmbedBuilder builder, Message message) {
        builder.setFooter(String.format("in #%s • %s",
            message.getTextChannel().getName(),
            TimeUtil.getDateTimeString(message.getTimeCreated())),
            null);
    }

    private void addImage(EmbedBuilder builder, Message message) {
        List<Attachment> attachments = message.getAttachments();
        Matcher urlFinder = MediaPatterns.URL_PATTERN.matcher(message.getContentDisplay());
        if (!attachments.isEmpty()) {
            builder.setImage(attachments.get(0).getUrl());
        } else if (urlFinder.find()) {
            String url = urlFinder.group(0);
            if (MediaPatterns.IMAGE_PATTERN.matcher(url).find()) {
                builder.setImage(url);
            }
        }
    }

    private int getRandomColor() {
        return (random.nextInt() * 16777214) + 1;
    }
}