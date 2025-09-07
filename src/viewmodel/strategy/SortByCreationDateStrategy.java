package viewmodel.strategy;

import model.task.ITask;

import java.util.Comparator;
import java.util.List;

<<<<<<< HEAD
public class SortByCreationDateStrategy implements SortingStrategy {
=======
/**
 * Sorting strategy that orders tasks by creation time from oldest to newest.
 */
public class SortByCreationDateStrategy implements SortingStrategy {

    /**
     * Sorts tasks by {@link ITask#getCreationDate()} in ascending order.
     *
     * @param tasks a mutable list of tasks; reordered in place
     */
>>>>>>> master
    @Override
    public void sort(List<ITask> tasks) {
        // Sort in ascending order (oldest to newest)
        tasks.sort(Comparator.comparing(ITask::getCreationDate));
<<<<<<< HEAD
=======

        System.out.println("Sorted Tasks by Creation Date:");
        for (ITask task : tasks) {
            System.out.println("  - " + task.toString());
        }
>>>>>>> master
    }
}
