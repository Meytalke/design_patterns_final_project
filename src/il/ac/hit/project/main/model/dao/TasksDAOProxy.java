package il.ac.hit.project.main.model.dao;

import il.ac.hit.project.main.model.task.ITask;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A proxy class that caches queries from the database to improve performance.
 * It implements the ITasksDAO interface and delegates queries to the underlying
 * ITasksDAO instance, while storing the results in a cache to avoid repeated
 * database queries.

 * <li>tasksDAO: The ITasksDAO instance to delegate queries to.</li>
 * <li>cache: A ConcurrentHashMap to store cached results.</li>

 */
public class TasksDAOProxy implements ITasksDAO {

    private final ITasksDAO tasksDAO;
    private final ConcurrentHashMap<Integer, ITask> cache = new ConcurrentHashMap<>();
    //Separate cache for all tasks - once gotten all the tasks at once, no need to retrieve separate tasks.
    private final ConcurrentHashMap<Integer, ITask> allCache = new ConcurrentHashMap<>();


    /**
     * Constructs a new TasksDAOProxy instance with the given
     * ITasksDAO instance to delegate queries to.
     * @param tasksDAO The ITasksDAO instance to delegate queries to.
     */
    public TasksDAOProxy(ITasksDAO tasksDAO) {this.tasksDAO = tasksDAO;}

    /**
     * Retrieves all tasks from the database or cache.
     *
     * @return {@code ITask[]} An array of all tasks in the database.
     * @throws TasksDAOException Occurs if there is a database access error when
     * retrieving tasks.
     */
    @Override
    public ITask[] getTasks() throws TasksDAOException {
        // Retrieve tasks from a cache
        if (!allCache.isEmpty()) {
            System.out.println("Returning tasks from cache.");
            System.out.println(allCache);
            return allCache.values().toArray(new ITask[0]);
        }
        // Retrieve tasks from a database and store them in a cache
        System.out.println("Fetching all tasks from DB.");
        ITask[] tasks = tasksDAO.getTasks();
        for (ITask task : tasks) {
            allCache.put(task.getId(), task);
        }
        return tasks;
    }

    /**
     * Retrieves a task from the database or cache.
     *
     * @param id The id of the task to retrieve.
     * @return The task with the given id, or null if the task does not exist.
     * @throws TasksDAOException If there is a database access error when retrieving a task.
     */
    @Override
    public ITask getTask(int id) throws TasksDAOException {
        //If we fetched all tasks once, no need to retrieve them again
        if (allCache.containsKey(id)) {
            System.out.println("Returning task from allCache: " + id);
            return allCache.get(id);
        }
        // In cache:
        if (cache.containsKey(id)) {
            System.out.println("Returning task from cache: " + id);
            return cache.get(id);
        }

        System.out.println("Fetching task " + id + " from DB.");
        ITask task = tasksDAO.getTask(id);
        //This if statement it is useless when getTask throws an exception when not found, so we can remove this part.
        if (task != null) {
            allCache.put(task.getId(), task);
            cache.put(task.getId(), task);
        }
        return task;
    }

    /**
     * Adds a task to the database and the cache.
     *
     * @param task The task to add.
     * @throws TasksDAOException If there is a database access error when adding a task.
     */
    @Override
    public void addTask(ITask task) throws TasksDAOException {
        tasksDAO.addTask(task);
        cache.put(task.getId(), task);
        allCache.put(task.getId(), task);
        System.out.println("Task added (cache): " + task.getId());
        System.out.println(cache);
        System.out.println(allCache);
    }

    /**
     * Deletes a task from the database and the cache.
     *
     * @param id The id of the task to delete.
     * @throws TasksDAOException If there is a database access error when deleting a task.
     */
    @Override
    public void deleteTask(int id) throws TasksDAOException {
        tasksDAO.deleteTask(id);
        //Remove from cache if exists
        cache.remove(id);
        allCache.remove(id);
        System.out.println("Task removed (cache): " + id);
    }

    /**
     * Updates a task in the database and the cache.
     *
     * @param task The task to update.
     * @throws TasksDAOException If there is a database access error when updating a task.
     */
    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        tasksDAO.updateTask(task);
        cache.put(task.getId(), task);
        System.out.println("Task updated (cache): " + task.getId());
    }

    /**
     * Deletes all tasks from the database and the cache.
     *
     * @throws TasksDAOException If there is a database access error when deleting all tasks.
     */
    @Override
    public void deleteTasks() throws TasksDAOException {
        tasksDAO.deleteTasks();
        cache.clear();
        allCache.clear();
    }
}
