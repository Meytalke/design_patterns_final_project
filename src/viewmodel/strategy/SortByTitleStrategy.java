package viewmodel.strategy;

import model.task.ITask;

import java.util.Comparator;
import java.util.List;

/**
 * Sorting strategy that orders tasks by their title in ascending, lexicographical order.
 */

public class SortByTitleStrategy implements ISortingStrategy {

    /**
     * Sorts tasks by {@link ITask#getTitle()} using String natural ordering.
     *
     * @param tasks a mutable list of tasks; reordered in place
     */
    @Override
    public void sort(List<ITask> tasks) {
        tasks.sort(Comparator.comparing(ITask::getTitle));
    }
}
