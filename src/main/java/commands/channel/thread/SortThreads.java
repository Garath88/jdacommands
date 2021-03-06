package commands.channel.thread;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commands.channel.database.ThreadsDbTable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utils.CategoryUtil;
import utils.GuildUtil;

public final class SortThreads {
    private static final Logger LOGGER = LoggerFactory.getLogger(SortThreads.class);
    private static AtomicInteger threadCounter = new AtomicInteger();
    private static final int MAX_HISTORY_LIMIT = 100;

    private SortThreads() {
    }

    public static void countUniquePostsAndSort(TextChannel thread, int amountOfThreads) {
        long threadId = thread.getIdLong();
        final long messageId;
        Long temp = ThreadsDbTable.getLatestMsgId(threadId);
        if (temp == 0 && thread.hasLatestMessage()) {
            temp = thread.getLatestMessageIdLong();
            ThreadsDbTable.storeLatestMsgId(temp, threadId);
        }
        if (temp > 0) {
            messageId = temp;
            //TODO: Fix async error
            try {
                countAndSortMessages(thread, messageId, amountOfThreads);
            } catch (Exception e) {
                String errorMsg = String.format("Failed to get latest msg id for %s using msgId: %s",
                    thread.getName(), messageId);
                LOGGER.error(errorMsg, e);
            }
        }
    }

    private static void countAndSortMessages(TextChannel thread, final long messageId, int amountOfThreads) {
        thread.retrieveMessageById(messageId).queue(
            latestMsg -> {
                int initialPostCount = ThreadsDbTable.getPostCount(thread);
                countSortAndDoInactivityTask(initialPostCount, latestMsg, thread, amountOfThreads);
            }
        );
    }

    private static void countSortAndDoInactivityTask(int postCount, Message latestMsg, TextChannel thread, int amountOfThreads) {
        try {
            thread.getHistoryAfter(latestMsg, MAX_HISTORY_LIMIT).queue(
                history -> {
                    long threadId = thread.getIdLong();
                    List<Message> messages = new ArrayList<>(history.getRetrievedHistory());
                    if (!messages.isEmpty()) {
                        messages.add(latestMsg);
                        int counter = postCount;
                        counter += countUniqueNewMessages(messages);
                        if (counter > 0) {
                            ThreadsDbTable.storePostCount(counter, threadId);
                        }
                        countSortAndDoInactivityTask(counter, messages.get(0), thread, amountOfThreads);
                    } else {
                        ThreadsDbTable.storeLatestMsgId(
                            latestMsg.getIdLong(), threadId);
                    }
                    LOGGER.debug("Post count for thread {} is {}",
                        thread.getName(), postCount);
                    boolean isLastThread = checkIfLastThread(amountOfThreads);
                    if (isLastThread) {
                        JDA jda = thread.getJDA();
                        sortAllThreadsByPostCountAndStartOrCancelInactivityTask(jda);
                    }
                });
        } catch (Exception e) {
            String errorMsg = String.format("Failed to count and store post count for %s",
                thread.getName());
            LOGGER.error(errorMsg, e);
        }
    }

    private static int countUniqueNewMessages(List<Message> messages) {
        ListIterator<Message> iter = messages.listIterator();
        int postCount = 0;
        if (messages.size() > 1) {
            postCount = countUniqueMessages(iter);
        }
        return postCount;
    }

    private static boolean checkIfLastThread(int amountOfThreads) {
        if (threadCounter.incrementAndGet() >= amountOfThreads) {
            threadCounter.set(0);
            return true;
        } else {
            return false;
        }
    }

    private static int countUniqueMessages(ListIterator<Message> iter) {
        int postCount = 0;
        while (iter.hasNext()) {
            Message latestMessage = iter.next();
            if (iter.hasNext()) {
                Message previousMessage = iter.next();
                User currentAuthor = latestMessage.getAuthor();
                User previousAuthor = previousMessage.getAuthor();
                if (!previousAuthor.isBot() && !currentAuthor.isBot() &&
                    (currentAuthor.getIdLong() != previousAuthor.getIdLong())
                    || !latestMessage.getAttachments().isEmpty()) {

                    postCount++;
                }
                iter.previous();
            }
        }
        return postCount;
    }

    static void sortAllThreadsByPostCountAndStartOrCancelInactivityTask(JDA jda) {
        Category threadCategory = CategoryUtil.getThreadCategory(jda);
        List<TextChannel> allThreads = threadCategory
            .getTextChannels();

        if (!allThreads.isEmpty()) {
            GuildUtil.getGuild(jda)
                .modifyTextChannelPositions(threadCategory)
                .sortOrder(Comparator.comparingInt(ThreadsDbTable::getGuildChannelPostCount)
                    .reversed())
                .queue(success ->
                    InactiveThreadChecker.startOrCancelInactivityTaskIfNotTopX(allThreads));
        }
    }

    public static void handleSortingOfThreads(GenericEvent event, TextChannel textChan) {
        if (CategoryUtil.getThreadCategory(event.getJDA()).getTextChannels().contains(textChan)) {
            if (event instanceof MessageReceivedEvent) {
                SortThreads.countUniquePostsAndSort(textChan, 1);
            } else if (event instanceof MessageDeleteEvent) {
                MessageDeleteEvent deleteEvent = ((MessageDeleteEvent)event);
                ThreadsDbTable.updateLatestMsgInDbIfDeleted(deleteEvent.getMessageIdLong(),
                    deleteEvent.getTextChannel());
            }
        }
    }
}
