package model.report;

import model.task.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor that aggregates task counts per state for reporting.
 * <p>
 * Each call to {@link #visit(ITask)} classifies a task by its current state and
 * increments the corresponding counter. After visiting the desired tasks, call
 * {@link #getReport()} to get an immutable snapshot of the counts.
 * <p>
 */
public class ReportVisitor implements TaskVisitor {
    private long completedTasks = 0;
    private long inProgressTasks = 0;
    private long todoTasks = 0;
    private final List<ITask> toDoTasksBucket = new ArrayList<>();
    private final List<ITask> inProgressTasksBucket = new ArrayList<>();
    private final List<ITask> completedTasksBucket = new ArrayList<>();

    /**
     * Classifies the given task by its state and updates internal counters.
     *
     * @param task the task to process; must not be null
     * @throws IllegalStateException if the task state is null or not recognized
     */
    @Override
    public void visit(ITask task) {
        ITaskState state = task.getState();
        switch (state) {
            case CompletedState _ -> {completedTasks++; completedTasksBucket.add(task);}
            case InProgressState _ -> {inProgressTasks++; inProgressTasksBucket.add(task);}
            case ToDoState _ -> {todoTasks++;toDoTasksBucket.add(task);}
            case null, default -> throw new IllegalStateException("Unknown task state: " + state);
        }
    }

    /**
     * Returns a snapshot of the aggregated counts so far.
     *
     * @return report data with counts of completed, in-progress, and to-do tasks
     */
    public ReportData getReport() {
        return new ReportData(completedTasks, inProgressTasks, todoTasks, completedTasksBucket, inProgressTasksBucket, toDoTasksBucket);
    }
}