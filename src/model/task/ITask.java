package model.task;

import model.report.TaskVisitor;
/**
 * Read-only contract for a task domain object.
 * <p>
 * Exposes identity, basic metadata, workflow state, and priority. This interface
 * does not define mutating operations; implementations may be immutable or provide
 * their own mutation APIs elsewhere.
 *
 * <p>Design notes:
 * <ul>
 *   <li>State transitions are modeled via {@link TaskState}.</li>
 *   <li>Reporting and export concerns are decoupled via the Visitor pattern
 *       (see {@link #accept(TaskVisitor)}).</li>
 * </ul>
 *
 * @see TaskState
 */
public interface ITask {

    /**
     * Returns the unique identifier of this task within its persistence context.
     *
     * @return the task id (implementation-defined uniqueness and range)
     */
    int getId();

    /**
     * Returns the short, human-readable title of the task.
     *
     * @return the non-null title string
     */
    String getTitle();

    /**
     * Returns a longer, possibly multi-line description of the task.
     *
     * @return the description, or {@code null} if not provided
     */
    String getDescription();

    /**
     * Returns the current workflow state of the task.
     *
     * @return the non-null task state
     */
    TaskState getState();

    /**
     * Accepts a visitor to perform an operation on this task without coupling
     * the task model to concrete reporting/export logic.
     * <p>
     * This follows the Visitor pattern (double dispatch). Implementations should
     * delegate to the appropriate {@code visit(...)} method on the given visitor.
     *
     * @param visitor the visitor to accept; must not be {@code null}
     * @throws NullPointerException if {@code visitor} is {@code null} (recommended behavior)
     */
    void accept(TaskVisitor visitor);
}