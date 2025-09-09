package il.ac.hit.project.main.model.dao;

/**
 * A checked exception that is thrown when there is an error in the {@link
 * il.ac.hit.project.main.model.dao.ITasksDAO} implementation.
 *
 */
public class TasksDAOException extends Exception {

    /**
     * Constructs a new {@code TasksDAOException} with the specified detail
     * message.
     *
     * @param message
     *            the detail message.
     */
    public TasksDAOException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code TasksDAOException} with the specified detail
     * message and cause.
     *
     * @param message
     *            the detail message.
     * @param cause
     *            the cause. (A {@code null} value is permitted, and indicates
     *            that the cause is nonexistent or unknown.)
     */
    public TasksDAOException(String message, Throwable cause) {
        super(message, cause);
    }

}
