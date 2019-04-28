package tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

public final class TaskListContainer {
    private final List<Task> tasks = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getTaskList() {
        return tasks;
    }

    public List<Task> getQueuedTasks() {
        return tasks
            .stream()
            .filter(Task::isNotRunning)
            .collect(Collectors.toList());
    }

    public void scheduleTasks() {
        getQueuedTasks().forEach(Task::scheduleTask);
    }

    public void cancelTasks() {
        tasks.forEach(TimerTask::cancel);
        tasks.clear();
    }

    public void cancelTask(Task task) {
        task.cancel();
        tasks.remove(task);
    }

    public void reScheduleTask(Task oldTask, Task newTask) {
        cancelTask(oldTask);
        addTask(newTask);
        newTask.scheduleTask();
    }
}
