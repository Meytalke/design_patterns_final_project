package viewmodel.strategy;

import model.task.ITask;

import java.util.Comparator;
import java.util.List;

<<<<<<< HEAD
public class SortByPriorityStrategy implements SortingStrategy {
=======
/**
 * Sorting strategy that orders tasks by priority from highest to lowest.
 */
public class SortByPriorityStrategy implements SortingStrategy {

    /**
     * Sorts tasks by {@link ITask#getPriority()} in descending order.
     *
     * @param tasks a mutable list of tasks; reordered in place
     */
>>>>>>> master
    @Override
    public void sort(List<ITask> tasks) {
        // Sort in descending order (HIGH to LOW)
        tasks.sort(Comparator.comparing(ITask::getPriority).reversed());
    }
}
