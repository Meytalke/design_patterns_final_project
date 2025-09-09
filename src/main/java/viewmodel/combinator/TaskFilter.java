package il.ac.hit.project.main.viewmodel.combinator;

import il.ac.hit.project.main.model.task.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Functional, composable filters for lists of {@link ITask}.
 * <p>
 * Combinators:
 * <ul>
 *   <li>{@link #and(TaskFilter)} and {@link #or(TaskFilter)} return new filters without shared mutable state.</li>
 *   <li>Factories: {@link #byId(int)}, {@link #byTitle(String)}, {@link #byDescription(String)}, {@link #byState(String)}.</li>
 * </ul>
 * <p>
 */
@FunctionalInterface
public interface TaskFilter {
    /**
     * Applies this filter to a list of tasks.
     *
     * @param tasks non-null input list (not mutated)
     * @return a new list containing tasks that match the filter; may be the same
     *         reference as {@code tasks} for identity filters
     */
    List<ITask> filter(List<ITask> tasks);

    /**
     * Creates a filter that matches tasks by exact id.
     *
     * @param id the task id to match
     * @return a filter selecting tasks where {@code task.getId() == id}
     */
    static TaskFilter byId(int id) {
        return tasks -> tasks.stream()
                .filter(task -> task.getId() == id)
                .collect(Collectors.toList());
    }

    /**
     * Creates a case-insensitive "contains" title filter.
     *
     * @param searchTerm non-null substring to look for in {@code getTitle()}
     * @return a filter selecting tasks whose title contains the term (case-insensitive)
     */
    static TaskFilter byTitle(String searchTerm) {
        String finalSearchTerm = searchTerm.toLowerCase();
        return tasks -> tasks.stream()
                .filter(task -> task.getTitle().toLowerCase().contains(finalSearchTerm))
                .collect(Collectors.toList());
    }

    /**
     * Creates a case-insensitive "contains" description filter.
     *
     * @param searchTerm non-null substring to look for in {@code getDescription()}
     * @return a filter selecting tasks whose description contains the term (case-insensitive)
     */
    static TaskFilter byDescription(String searchTerm) {
        String finalSearchTerm = searchTerm.toLowerCase();
        return tasks -> tasks.stream()
                .filter(task -> task.getDescription().toLowerCase().contains(finalSearchTerm))
                .collect(Collectors.toList());
    }

    /**
     * Creates a filter that matches tasks by their workflow state display name.
     *
     * @param state one of "To Do", "In Progress", "Completed", or "All"/null for no filtering
     * @return a filter selecting tasks in the given state
     * @throws IllegalArgumentException if {@code state} is not recognized
     */
    static TaskFilter byState(String state) {
        if (state == null || "All".equalsIgnoreCase(state)) {
            return tasks -> tasks;
        }

        TaskState taskState;
        switch (state) {
            //Use the linking of object instances to save in memory (at most 3 states instances)
            case "To Do" -> taskState = new ToDoState();
            case "In Progress" -> taskState = new InProgressState(new ToDoState());
            case "Completed" -> taskState = new CompletedState(new InProgressState(new ToDoState()));
            default -> throw new IllegalArgumentException("Unknown state: " + state);
        }

        return tasks -> tasks.stream()
                .filter(task -> task.getState().getDisplayName().equals(taskState.getDisplayName()))
                .collect(Collectors.toList());
    }

    /**
     * Logical conjunction (AND) combinator.
     * <p>
     * Applies {@code this} filter first, then {@code other} to the intermediate result.
     * The result preserves the order produced by the left-hand filter.
     *
     * @param other another filter to apply after this one
     * @return a composed filter equivalent to {@code other(this(tasks))}
     */
    default TaskFilter and(TaskFilter other) {
        return tasks -> other.filter(this.filter(tasks));
    }

    /**
     * Logical disjunction (OR) combinator.
     * <p>
     * Returns the union of results from {@code this} and {@code other}, de-duplicated
     * via a {@link Set}. Ordering is not guaranteed.
     *
     * @param other another filter to union with this one
     * @return a composed filter equivalent to {@code set(this(tasks)) ∪ set(other(tasks))}
     */
    default TaskFilter or(TaskFilter other) {
        return tasks -> {
            Set<ITask> combinedResults = new HashSet<>();
            combinedResults.addAll(this.filter(tasks));
            combinedResults.addAll(other.filter(tasks));
            return new ArrayList<>(combinedResults);
        };
    }
}