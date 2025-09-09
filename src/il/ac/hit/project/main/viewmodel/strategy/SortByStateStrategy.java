package il.ac.hit.project.main.viewmodel.strategy;

import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.TaskState;
import il.ac.hit.project.main.model.task.ToDoState;
import il.ac.hit.project.main.model.task.InProgressState;
import il.ac.hit.project.main.model.task.CompletedState;

import java.util.Comparator;
import java.util.List;

/**
 * Sorting strategy that orders tasks by their current workflow state.
 */
public class SortByStateStrategy implements ISortingStrategy {

    /**
     * Sorts tasks by a predefined state order: ToDo, InProgress, Completed.
     *
     * @param tasks a mutable list of tasks; reordered in place
     */
    @Override
    public void sort(List<ITask> tasks) {
        tasks.sort(new Comparator<ITask>() {
            @Override
            public int compare(ITask task1, ITask task2) {
                return Integer.compare(getRank(task1.getState()), getRank(task2.getState()));
            }

            private int getRank(TaskState state) {
                if (state instanceof ToDoState) {
                    return 1;
                }
                if (state instanceof InProgressState) {
                    return 2;
                }
                if (state instanceof CompletedState) {
                    return 3;
                }
                return 4;
            }
        });
    }
}
