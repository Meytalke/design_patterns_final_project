package view;

import model.task.ITask;

import java.util.List;

/**
 * An interface for objects that can observe changes to the list of tasks.
 */
public interface TasksObserver {
   
    /**
     * Called when the list of tasks has been updated.
     * @param tasks The new, updated list of tasks.
     */
    void onTasksChanged(List<ITask> tasks);
}
