package il.ac.hit.project.main.model.dao;

import il.ac.hit.project.main.model.task.ITask;

/**
 * Data-access contract for working with tasks in a persistence layer.
 * <p>
 * Implementations may use databases, files, or in-memory stores, but must:
 * <ul>
 *   <li>Never return {@code null} arrays (use an empty array when there are no tasks).</li>
 *   <li>Throw {@link TasksDAOException} to indicate persistence-related failures.</li>
 * </ul>
 */
public interface ITasksDAO {

    /**
     * Retrieves all tasks from the data store.
     *
     * @return a non-null array containing all tasks; the array is empty if no tasks exist
     * @throws TasksDAOException if the tasks cannot be retrieved due to a persistence error
     */
    ITask[] getTasks() throws TasksDAOException;

    /**
     * Retrieves a single task by its identifier.
     *
     * @param id the unique identifier of the task to retrieve
     * @return the task with the given id, or {@code null} if no such task exists
     * @throws TasksDAOException if the task cannot be retrieved due to a persistence error
     */
    ITask getTask(int id) throws TasksDAOException;

    /**
     * Persists a new task.
     *
     * @param task the task to add; must not be {@code null}
     * @throws TasksDAOException if the task cannot be added (e.g., due to I/O/database errors
     *                           or a constraint violation such as a duplicate identifier)
     */
    void addTask(ITask task) throws TasksDAOException;

    /**
     * Updates an existing task in the data store.
     *
     * @param task the task to update; must not be {@code null}
     * @throws TasksDAOException if the task cannot be updated (e.g., it does not exist
     *                           or a persistence error occurs)
     */
    void updateTask(ITask task) throws TasksDAOException;

    /**
     * Deletes all tasks from the data store.
     *
     * @throws TasksDAOException if the operation fails due to a persistence error
     */
    void deleteTasks() throws TasksDAOException;

    /**
     * Deletes the task with the specified identifier.
     *
     * <p>The operation is expected to be idempotent: if no task with the given id exists,
     * the method should complete without effect.</p>
     *
     * @param id the unique identifier of the task to delete
     * @throws TasksDAOException if the task cannot be deleted due to a persistence error
     */
    void deleteTask(int id) throws TasksDAOException;
}