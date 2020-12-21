package commands.bump;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import commands.Permissions;

public final class BumpAddCommand extends Command {
    private static final Pattern VALID_PATTERN =
        Pattern.compile("(^(?! )[-!$%^&*()_+|~=`{}\\[\\]:\";'<>?,./ a-zA-z]+(?<=\\S)) (\\d+)");
    private static final int NUMBER_OF_ARGUMENTS = 2;
    private static final int NUMBER_ARGUMENT_INDEX = 1;

    public BumpAddCommand() {
        this.name = "bump_add";
        this.help = "Add a command with a timer (in minutes) to bump.";
        this.arguments = "<command> <timer>";
        this.requiredRoles = Permissions.MODERATOR.getValues();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String args = event.getArgs();
            String[] arguments = splitArguments(args);
            validateCorrectInput(arguments);
            BumpTaskList.getTaskListContainer()
                .addTask(new BumpTask(arguments[0], Integer.valueOf(arguments[1]), event));
            if (BumpCommand.isRunning()) {
                BumpCommand.scheduleBump(event);
            }
            event.reply(
                String.format("Successfully added command \"**%s**\" with a **%s** minute timer",
                    arguments[0], arguments[1]));
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private String[] splitArguments(String args) {
        List<String> chunks = new LinkedList<>();
        Matcher matcher = VALID_PATTERN.matcher(args);
        while (matcher.find()) {
            chunks.add(matcher.group(1));
            chunks.add(matcher.group(2));
        }
        return chunks.toArray(new String[0]);
    }

    private void validateCorrectInput(String[] arguments) {
        if (ArrayUtils.isNotEmpty(arguments)) {
            Preconditions.checkArgument(arguments.length == NUMBER_OF_ARGUMENTS, "Invalid number of arguments!");
            Preconditions.checkArgument(StringUtils.isNumeric(arguments[NUMBER_ARGUMENT_INDEX]), "Timer argument is not numeric!");
        } else {
            throw new IllegalArgumentException("Invalid input");
        }
    }
}
