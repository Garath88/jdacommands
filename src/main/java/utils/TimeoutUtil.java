package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TimeoutUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtil.class);

    private TimeoutUtil() {
    }

    static void setTimeout(Runnable runnable, int delayInMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delayInMillis);
                runnable.run();
            } catch (Exception e) {
                LOGGER.error("Failed to use timeout", e);
            }
        }).start();
    }
}
