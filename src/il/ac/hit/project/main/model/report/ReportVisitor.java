package il.ac.hit.project.main.model.report;

import il.ac.hit.project.main.model.task.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor that aggregates a task counting by state for reporting.
 * <p>
 * This class uses a modern approach to the Visitor pattern by processing
 * specific object types, rather than relying on a common visitor interface
 * in the visited objects. This design improves decoupling and scalability.
 * <p>
 * To use this visitor, pass it instances of {@link Task} directly.
 * After visiting all desired tasks, call {@link #getReport()} to
 * retrieve an immutable summary of the counts.
 */
public class ReportVisitor {
    private long completedTasks = 0;
    private long inProgressTasks = 0;
    private long todoTasks = 0;
    private final List<ITask> toDoTasksBucket = new ArrayList<>();
    private final List<ITask> inProgressTasksBucket = new ArrayList<>();
    private final List<ITask> completedTasksBucket = new ArrayList<>();

    /**
     * Classifies a given task by its state and updates the internal counters.
     * <p>
     * This method is designed to be called directly by client code that
     * iterates over a collection of tasks and uses pattern matching to
     * determine the correct visit method.
     *
     * @param task the task to process; must not be null
     * @see #getReport()
     * @throws IllegalStateException if the task state is null or not recognized
     */
    public void visit(Task task) {
        TaskState state = task.getState();
        switch (state) {
            case CompletedState _ -> {completedTasks++; completedTasksBucket.add(task);}
            case InProgressState _ -> {inProgressTasks++; inProgressTasksBucket.add(task);}
            case ToDoState _ -> {todoTasks++;toDoTasksBucket.add(task);}
            case null, default -> throw new IllegalStateException("Unknown task state: " + state);
        }
    }

    /**
     * Returns an immutable snapshot of the aggregated counts.
     *
     * @return a {@link ReportData} record containing the counts of
     * completed, in-progress, and to-do tasks.
     */
    public ReportData getReport() {
        return new ReportData(completedTasks, inProgressTasks, todoTasks, completedTasksBucket, inProgressTasksBucket, toDoTasksBucket);
    }
}