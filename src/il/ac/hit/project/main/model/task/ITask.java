package il.ac.hit.project.main.model.task;

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
}