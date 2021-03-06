package commands.channel.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.MariaDbConnector;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;


/*TODO: TEST JOOQ OR USE PREPARED STATEMENTS INSTEAD OF THIS*/

public final class ThreadsDbTable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadsDbTable.class);
    private static final String TABLE_NAME = "threads";
    private static final String QUERY_RESULT_ERROR = "Failed to close or get result from query";
    private static final String DB_NAME = MariaDbConnector.getConfig().getDbName();

    private ThreadsDbTable() {
    }

    public static void addThread(User user, TextChannel thread) {
        String sql = String.format("INSERT INTO %s.%s "
                + "(user,user_id, name, id) "
                + "VALUES ('%s', '%s', '%s', '%s')",
            DB_NAME, TABLE_NAME,
            user.getAsTag(), user.getId(), thread.getName(), thread.getId());
        SqlUtil.executeQuery(sql);
    }

    private static List<Long> getAllChannelIds() {
        String sql = String.format("SELECT id FROM %s.%s",
            DB_NAME, TABLE_NAME);
        ResultSet result = MariaDbConnector.executeSql(sql);
        List<Long> channelIds = new ArrayList<>();
        if (result != null) {
            try {
                while (result.next()) {
                    channelIds.add(result.getLong("id"));
                }
                result.close();
            } catch (SQLException e) {
                LOGGER.error(QUERY_RESULT_ERROR, e);
            }
        }
        return channelIds;
    }

    public static ChannelDbInfo getThreadInfoFromUser(User user) {
        String sql = String.format(
            "SELECT name, id FROM %s.%s WHERE user_id = %s",
            DB_NAME, TABLE_NAME, user.getId());
        ResultSet result = MariaDbConnector.executeSql(sql);
        List<String> outprint = new ArrayList<>();
        List<Long> channelIds = new ArrayList<>();
        int count = 0;
        if (result != null) {
            try {
                while (result.next()) {
                    count++;
                    outprint.add((String.format("`[%d]`  **%-2s**",
                        count, result.getString("name"))));
                    channelIds.add(result.getLong("id"));
                }
                result.close();
                return new ChannelDbInfo(String.join("\n", outprint), channelIds);
            } catch (SQLException e) {
                LOGGER.error(QUERY_RESULT_ERROR, e);
            }
        }
        return new ChannelDbInfo("No created channels found!",
            Collections.emptyList());
    }

    public static void deleteChannel(Long id) {
        String sql = String.format(
            "DELETE FROM %s.%s WHERE id = %s", DB_NAME, TABLE_NAME, id);
        SqlUtil.executeQuery(sql);
    }

    public static void checkForThreadDbInconsistency(List<TextChannel> threads) {
        List<Long> allThreadsById = threads.stream()
            .map(ISnowflake::getIdLong)
            .collect(Collectors.toList());
        List<Long> deletedChannels = ThreadsDbTable.getAllChannelIds().stream()
            .filter(chanId -> !allThreadsById
                .contains(chanId))
            .collect(Collectors.toList());
        deletedChannels.forEach(ThreadsDbTable::deleteChannel);
        threads.forEach(ThreadsDbTable::checkStoredLatestMessage);
    }

    private static void checkStoredLatestMessage(TextChannel thread) {
        long messageId = getLatestMsgId(thread.getIdLong());
        if (messageId != 0) {
            thread.retrieveMessageById(messageId)
                .queue(success -> {
                }, fail -> updateLatestMsgInDbIfDeleted(messageId, thread));
        }
    }

    public static void updateLatestMsgInDbIfDeleted(long deletedMsgId, TextChannel textChan) {
        if (deletedMsgId == ThreadsDbTable.getLatestMsgId(textChan.getIdLong())) {
            updateToPreviousMsg(deletedMsgId, textChan);
        }
    }

    private static void updateToPreviousMsg(long deletedMsgId, TextChannel textChan) {
        long textChanId = textChan.getIdLong();
        textChan.getHistoryBefore(deletedMsgId, 1).queue(history -> {
            List<Message> messages = history.getRetrievedHistory();
            if (!messages.isEmpty()) {
                ThreadsDbTable.storeLatestMsgId(
                    messages.get(0).getIdLong(), textChanId);
            } else {
                ThreadsDbTable.storeLatestMsgId(0, textChanId);
            }
        });
    }

    public static void storeLatestMsgId(long messageId, long threadId) {
        String sql = String.format("UPDATE %s.%s "
                + "SET latest_message_id = %s WHERE id = %s ",
            DB_NAME, TABLE_NAME, messageId, threadId);
        SqlUtil.executeQuery(sql);
    }

    public static long getLatestMsgId(long threadId) {
        String sql = String.format(
            "SELECT latest_message_id FROM %s.%s WHERE id = %s",
            DB_NAME, TABLE_NAME, threadId);
        ResultSet result = MariaDbConnector.executeSql(sql);
        long latestMessageId = 0;
        if (result != null) {
            try {
                while (result.next()) {
                    latestMessageId = result.getLong("latest_message_id");
                }
                result.close();
            } catch (SQLException e) {
                LOGGER.error(QUERY_RESULT_ERROR, e);
            }
        }
        return latestMessageId;
    }

    public static int getPostCount(TextChannel thread) {
        long threadId = thread.getIdLong();
        return getPostCount(threadId);
    }

    public static int getGuildChannelPostCount(GuildChannel thread) {
        long threadId = thread.getIdLong();
        return getPostCount(threadId);
    }

    private static int getPostCount(long threadId) {
        String sql = String.format(
            "SELECT post_count FROM %s.%s WHERE id = %s",
            DB_NAME, TABLE_NAME, threadId);
        ResultSet result = MariaDbConnector.executeSql(sql);
        int postCount = 0;
        if (result != null) {
            try {
                while (result.next()) {
                    postCount = result.getInt("post_count");
                }
                result.close();
            } catch (SQLException e) {
                LOGGER.error(QUERY_RESULT_ERROR, e);
            }
        }
        LOGGER.trace("Post count for thread {} is {}",
            threadId, postCount);
        return postCount;
    }

    public static void storePostCount(int postCount, long threadId) {
        String sql = String.format("UPDATE %s.%s "
                + "SET post_count = %s WHERE id = %s ",
            DB_NAME, TABLE_NAME, postCount, threadId);
        SqlUtil.executeQuery(sql);
    }

    public static int getBumpCount(long threadId) {
        String sql = String.format(
            "SELECT bump_count FROM %s.%s WHERE id = %s",
            DB_NAME, TABLE_NAME, threadId);
        ResultSet result = MariaDbConnector.executeSql(sql);
        int bumpCount = 0;
        if (result != null) {
            try {
                while (result.next()) {
                    bumpCount = result.getInt("bump_count");
                }
                result.close();
            } catch (SQLException e) {
                LOGGER.error(QUERY_RESULT_ERROR, e);
            }
        }
        return bumpCount;
    }

    public static void storeBumpCount(int bumpCount, long threadId) {
        String sql = String.format("UPDATE %s.%s "
                + "SET bump_count = %s WHERE id = %s ",
            DB_NAME, TABLE_NAME, bumpCount, threadId);
        SqlUtil.executeQuery(sql);
    }
}