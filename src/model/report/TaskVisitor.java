package model.report;

import model.task.ITask;

/**
 * Visitor for processing tasks during traversal/aggregation.
 * Implementations can perform reporting, validation, or collection of metrics
 * without changing the visited tasks.
 */
public interface TaskVisitor {

    /**
     * Processes a single task.
     *
     * @param task the task to visit; must not be null
     */
    void visit(ITask task);
}