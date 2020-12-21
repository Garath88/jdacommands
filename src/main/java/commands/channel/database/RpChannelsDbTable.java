package commands.channel.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.MariaDbConnector;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class RpChannelsDbTable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadsDbTable.class);
    private static final String TABLE_NAME = "rpchannels";
    private static final String QUERY_RESULT_ERROR = "Failed to close or get result from query";
    private static final String DB_NAME = MariaDbConnector.getConfig().getDbName();

    private RpChannelsDbTable() {
    }

    public static void addChannel(User user, TextChannel channel) {
        String sql = String.format("INSERT INTO %s.%s "
                + "(user,user_id, name, id) "
                + "VALUES ('%s', '%s', '%s', '%s')",
            DB_NAME, TABLE_NAME,
            user.getAsTag(), user.getId(), channel.getName(), channel.getId());
        SqlUtil.executeQuery(sql);
    }

    public static void deleteChannel(Long id) {
        String sql = String.format(
            "DELETE FROM %s.%s WHERE id = %s", DB_NAME, TABLE_NAME, id);
        SqlUtil.executeQuery(sql);
    }

    public static ChannelDbInfo getChannelInfoFromUser(User user) {
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
}
