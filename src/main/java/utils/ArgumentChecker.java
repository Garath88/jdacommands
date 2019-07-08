package utils;

import org.apache.commons.lang3.StringUtils;

public final class ArgumentChecker {
    private static final String SPLIT_BY_SPACE = "\\s+";
    private static final String SPIT_BY_PIPE = "\\|+";
    private static final String INVALID_ARGS_MSG = "Wrong number of arguments, expected: %d";
    private static final String INVALID_MAX_ARGS_MSG = "Wrong number of arguments, max is: %d";

    private ArgumentChecker() {
    }

    public static void checkArgsBySpaceRequires(String arguments, int requiredArguments) {
        if (getNumberOfArguments(arguments, SPLIT_BY_SPACE) != requiredArguments) {
            throw new IllegalArgumentException(String.format(INVALID_ARGS_MSG,
                requiredArguments));
        }
    }

    public static void checkArgsBySpaceIsAtLeast(String arguments, int minimunRequiredArguments) {
        if (getNumberOfArguments(arguments, SPLIT_BY_SPACE) < minimunRequiredArguments) {
            throw new IllegalArgumentException(String.format(INVALID_ARGS_MSG,
                minimunRequiredArguments));
        }
    }

    public static void checkArgsBySpaceIsAtMax(String arguments, int maxRequiredArguments) {
        if (getNumberOfArguments(arguments, SPLIT_BY_SPACE) > maxRequiredArguments) {
            throw new IllegalArgumentException(String.format(INVALID_MAX_ARGS_MSG,
                maxRequiredArguments));
        }
    }

    public static void checkArgsByPipe(String arguments, int requiredArguments) {
        if (getNumberOfArguments(arguments, SPIT_BY_PIPE) != requiredArguments) {
            throw new IllegalArgumentException(String.format(INVALID_ARGS_MSG,
                requiredArguments));
        }
    }

    private static int getNumberOfArguments(String arguments, String regex) {
        String[] items = arguments.split(regex);
        if (StringUtils.isEmpty(arguments)) {
            return 0;
        }
        return items.length;
    }

    public static void checkIfArgsAreNotEmpty(String arguments) {
        if (StringUtils.isEmpty(arguments)) {
            throw new IllegalArgumentException("You didn't give me any arguments!");
        }
    }
}


