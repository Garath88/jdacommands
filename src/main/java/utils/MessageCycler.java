package utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.dv8tion.jda.core.entities.MessageChannel;

public final class MessageCycler {
    private List<String> responses;
    private Map<String, List<String>> responsesByKey = new ConcurrentHashMap<>();
    private long delayBetweenResponsesInMilli;
    private int nextMessageCounter = 0;
    private long startTime = System.currentTimeMillis();
    private boolean repeat = true;

    public MessageCycler(List<String> responses, long delayBetweenResponsesInMilli, boolean repeat) {
        this.responses = responses;
        this.delayBetweenResponsesInMilli = delayBetweenResponsesInMilli;
        this.repeat = repeat;
    }

    public MessageCycler(Map<String, List<String>> responsesByKey, long delayBetweenResponsesInMilli) {
        this.responsesByKey = responsesByKey;
        this.delayBetweenResponsesInMilli = delayBetweenResponsesInMilli;
    }

    public void replyWithMessageAndDeleteAfterDelay(MessageChannel channel, int delayInMillis) {
        MessageUtil.sendMessageToChannelAndDelete(getNextMessageAfterTimer(), channel, delayInMillis);
    }

    public String getNextMessageAfterTimer() {
        String ret = "";
        long endTime = System.currentTimeMillis();
        long millisecondsPassed = endTime - startTime;
        if (millisecondsPassed >= delayBetweenResponsesInMilli) {
            if (nextMessageCounter >= responses.size()) {
                if (repeat) {
                    nextMessageCounter = 0;
                } else {
                    nextMessageCounter = responses.size() - 1;
                }
            }
            ret = responses.get(nextMessageCounter);
            nextMessageCounter++;
            startTime = System.currentTimeMillis();
        }
        return ret;
    }

    public String getMessageByKey(String key) {
        return getNextMessageAfterTimer(key);
    }

    private String getNextMessageAfterTimer(String key) {
        String ret = "";
        long endTime = System.currentTimeMillis();
        long millisecondsPassed = endTime - startTime;
        if (millisecondsPassed >= delayBetweenResponsesInMilli) {
            if (nextMessageCounter == responsesByKey.size()) {
                nextMessageCounter = 0;
            }
            ret = responsesByKey.get(key).get(nextMessageCounter);
            nextMessageCounter++;
            startTime = System.currentTimeMillis();
        }
        return ret;
    }

    public void resetMessageCounter() {
        nextMessageCounter = 0;
    }
}
