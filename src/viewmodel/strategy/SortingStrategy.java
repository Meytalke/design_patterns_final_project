package viewmodel.strategy;

import model.task.ITask;

import java.util.List;

<<<<<<< HEAD
public interface SortingStrategy {
=======
/**
 * Strategy contract for sorting task collections.
 * <p>
 * Implementations define a specific ordering criterion and are free to sort
 * the provided list in place.
 */
public interface SortingStrategy {
    /**
     * Sorts the provided list of tasks according to this strategy's ordering.
     *
     * @param tasks a mutable, non-null list of tasks to be sorted in place
     *              (the reference and its contents may be reordered)
     * @throws NullPointerException if {@code tasks} is null
     */

>>>>>>> master
    void sort(List<ITask> tasks);
}
