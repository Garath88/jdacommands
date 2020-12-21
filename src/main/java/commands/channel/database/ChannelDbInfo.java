package commands.channel.database;

import java.util.List;

public class ChannelDbInfo {
    private String listedThreads;
    private List<Long> threadIds;

    ChannelDbInfo(String listedThreads, List<Long> threadIds) {
        this.listedThreads = listedThreads;
        this.threadIds = threadIds;
    }

    public String getlistedChannels() {
        return listedThreads;
    }

    public List<Long> getThreadIds() {
        return threadIds;
    }
}