package il.ac.hit.project.main.viewmodel.strategy;

import il.ac.hit.project.main.model.task.ITask;

import java.util.Comparator;
import java.util.List;

/**
 * Sorting strategy that orders tasks by their unique identifier (ID).
 */
public class SortByIDStrategy implements ISortingStrategy {

    /**
     * Sorts tasks by ID in ascending order.
     *
     * @param tasks a mutable list of tasks; reordered in place
     */
    @Override
    public void sort(List<ITask> tasks) {
        // Sort in ascending order (the smallest to largest ID)
        tasks.sort(Comparator.comparing(ITask::getId));

        System.out.println("Sorted Tasks by ID:");
        for (ITask task : tasks) {
            System.out.println("  - " + task.toString());
        }
    }
}