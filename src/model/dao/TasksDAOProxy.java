package model.dao;

import model.task.ITask;

import java.util.concurrent.ConcurrentHashMap;

public class TasksDAOProxy implements ITasksDAO {

    private final ITasksDAO realDAO;
    private ConcurrentHashMap<Integer, ITask> cache = new ConcurrentHashMap<>();

    public TasksDAOProxy(ITasksDAO realDAO) {
        this.realDAO = realDAO;
    }

    @Override
    public ITask[] getTasks() throws TasksDAOException {
        if (!cache.isEmpty()) {
            System.out.println("Returning tasks from cache.");
            System.out.println(cache);
            return cache.values().toArray(new ITask[0]);
        }

        System.out.println("Fetching all tasks from DB.");
        ITask[] tasks = realDAO.getTasks();
        for (ITask task : tasks) {
            cache.put(task.getId(), task);
        }
        return tasks;
    }

    @Override
    public ITask getTask(int id) throws TasksDAOException {
        if (cache.containsKey(id)) {
            System.out.println("Returning task from cache: " + id);
            return cache.get(id);
        }

        System.out.println("Fetching task " + id + " from DB.");
        ITask task = realDAO.getTask(id);
        if (task != null) {
            cache.put(task.getId(), task);
        }
        return task;
    }

    @Override
    public void addTask(ITask task) throws TasksDAOException {
        realDAO.addTask(task);
        if (!cache.isEmpty()) {
            cache.put(task.getId(), task);
            System.out.println("Task added (cache): " + task.getId());
        }
    }

    @Override
    public void deleteTask(int id) throws TasksDAOException {
        realDAO.deleteTask(id);
        cache.remove(id);
        System.out.println("Task removed (cache): " + id);
    }

    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        realDAO.updateTask(task);
        cache.put(task.getId(), task);
        System.out.println("Task updated (cache): " + task.getId());
    }

    @Override
    public void deleteTasks() throws TasksDAOException {
        realDAO.deleteTasks();
        cache.clear();
    }
}
