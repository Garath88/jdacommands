package commands.bump;

import tasks.TaskListContainer;

final class BumpTaskList {
    private static TaskListContainer taskListContainer = new TaskListContainer();

    private BumpTaskList() {
    }

    static TaskListContainer getTaskListContainer() {
        return taskListContainer;
    }
}