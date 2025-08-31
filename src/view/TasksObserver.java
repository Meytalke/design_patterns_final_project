package view;

import model.task.ITask;

import java.util.List;

public interface TasksObserver {
    /**
     * Called by the ViewModel when the list of tasks has been updated.
     * @param tasks The new, updated list of tasks.
     */
    void onTasksChanged(List<ITask> tasks);
}
