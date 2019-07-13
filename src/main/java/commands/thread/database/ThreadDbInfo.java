package commands.thread.database;

import java.util.List;

public class ThreadDbInfo {
    private String listedThreads;
    private List<Long> threadIds;

    ThreadDbInfo(String listedThreads, List<Long> threadIds) {
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
