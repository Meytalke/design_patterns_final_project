package viewmodel;

import model.dao.ITasksDAO;
import model.dao.TasksDAOException;
import model.report.*;
import model.task.*;
import view.TasksObserver;
import view.IView;
import viewmodel.combinator.TaskFilter;
import viewmodel.strategy.SortByCreationDateStrategy;
import viewmodel.strategy.SortingStrategy;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ViewModel that coordinates between the View and the Tasks data layer in an MVVM architecture.
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Asynchronously load, add, update, and delete tasks using {@link ITasksDAO}.</li>
 *   <li>Maintain an in-memory collection of all tasks and a working list that can be filtered and sorted.</li>
 *   <li>Notify registered {@link TasksObserver} instances whenever the visible tasks list changes.</li>
 *   <li>Apply sorting via pluggable {@link SortingStrategy} implementations.</li>
 *   <li>Filter tasks using combinable predicates from {@link TaskFilter}.</li>
 *   <li>Generate reports using a Visitor + Adapter approach ({@link ReportVisitor}, {@link IReportExporter}).</li>
 * </ul>
 *
 * <h3>Threading</h3>
 * <ul>
 *   <li>DAO interactions are executed on a background {@link ExecutorService}.</li>
 *   <li>{@link #notifyObservers()} is called from the thread that performs the change; in this class
 *       that is typically a worker thread.</li>
 *   <li>The class is not designed for concurrent external mutation; callers should not modify returned lists.</li>
 *   <li>If the UI must be updated on a specific thread, observers should marshal to that thread.</li>
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *   <li>Provide the DAO and view via the constructor or via setters.</li>
 *   <li>Invoke {@link #shutdown()} when the ViewModel is no longer needed to stop the executor.</li>
 * </ul>
 *
 * @see IView
 * @see ITasksDAO
 * @see ITask
 * @see SortingStrategy
 * @see TaskFilter
 */
public class TasksViewModel implements IViewModel {

    // The currently attached view. May be null until explicitly set.
    private IView view;

    // The data-access object used to persist and retrieve tasks.
    private ITasksDAO tasksDAO;

    /**
     * Observer list that gets notified whenever {@link #tasks} changes.
     * This list will contain observers to UI components that need to be updated
     * to start, we will update the whole UI everytime the list of tasks changes
     */
    private final List<TasksObserver> observers = new ArrayList<>();

    // The current working list of tasks, after filtering and sorting.
    private List<ITask> tasks = new ArrayList<>();

    // The complete in-memory set of tasks last loaded from the DAO.
    private List<ITask> allTasks = new ArrayList<>();

    // Registry of report exporters keyed by human-readable format (e.g., "PDF", "CSV").
    private final Map<String, IReportExporter> exporters = new HashMap<>();

    // The active sorting strategy used to order {@link #tasks}.
    private SortingStrategy currentSortingStrategy;

    // Executor used to perform DAO operations and other background work.
    private final ExecutorService service;

    //private ObservableProperty<List<ITask>> listObservers = new ObservableProperty<>(tasks);

    /**
     * Creates a new TasksViewModel, wires the DAO and the View, registers default exporters,
     * sets the default sorting strategy, and triggers an initial asynchronous load of tasks.
     *
     * @param tasksDAO the DAO used for persistence; must not be null
     * @param view     the view to associate; must not be null
     */
    public TasksViewModel(ITasksDAO tasksDAO, IView view) {
        setModel(tasksDAO);
        setView(view);
        exporters.put("Terminal", new ReportAdapter());
        exporters.put("PDF", new PdfReportAdapter());
        exporters.put("CSV", new CsvReportAdapter());
        exporters.put("JSON", new JsonReportAdapter());
        this.currentSortingStrategy = new SortByCreationDateStrategy();
        this.service = Executors.newFixedThreadPool(8);
        loadTasks(); // Initial load
    }

    /**
     * Registers a {@link TasksObserver} to receive updates when the visible task list changes.
     *
     * @param observer the observer to add; must not be null
     */
    @Override
    public void addObserver(TasksObserver observer) {
        observers.add(observer);
    }

    /**
     * Unregisters a previously registered {@link TasksObserver}.
     *
     * @param observer the observer to remove; must not be null
     */
    @Override
    public void removeObserver(TasksObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers that the visible task list has changed.
     * Observers are invoked on the calling thread.
     */
    @Override
    public void notifyObservers() {
        for (TasksObserver observer : observers) {
            observer.onTasksChanged(tasks);
        }
    }

    /**
     * Associates this ViewModel with a view.
     *
     * @param view the view to attach; must not be null
     */
    @Override
    public void setView(IView view){
        this.view = view;
    }

    /**
     * Sets the data-access object used by this ViewModel.
     *
     * @param tasksDAO the DAO to use; must not be null
     */
    @Override
    public void setModel(ITasksDAO tasksDAO) {
        this.tasksDAO = tasksDAO;
    }

    /**
     * Returns the currently attached view, or null if none has been set.
     *
     * @return the view, or null
     */
    @Override
    public IView getView() {
        return view;
    }

    /**
     * Returns the current data-access object, or null if none has been set.
     *
     * @return the DAO, or null
     */
    @Override
    public ITasksDAO getModel() {
        return tasksDAO;
    }

    /**
     * Sets the active sorting strategy and immediately re-sorts the current visible task list.
     * Observers are notified after sorting.
     *
     * @param strategy the sorting strategy to apply; must not be null
     */
    public void setSortingStrategy(SortingStrategy strategy) {
        this.currentSortingStrategy = strategy;
        sortTasks(); // Re-sort the current list of tasks
        notifyObservers();
    }

    /**
     * Applies the current sorting strategy to the visible task list, if any.
     * No-op if there is no strategy or the list is empty.
     */
    private void sortTasks() {
        if (currentSortingStrategy != null && !tasks.isEmpty()) {
            currentSortingStrategy.sort(this.tasks);
        }
    }

    /**
     * Asynchronously loads all tasks from the DAO, replaces the in-memory lists,
     * and notifies observers. Errors are logged to stderr.
     */
    public void loadTasks() {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                ITask[] tasksArray = tasksDAO.getTasks();
                // Convert the array to an ArrayList for mutable operations
                this.allTasks = new ArrayList<>(Arrays.asList(tasksArray));
                this.tasks = new ArrayList<>(this.allTasks);
                notifyObservers();
            } catch (TasksDAOException e){
                System.err.println("Error loading tasks: " + e.getMessage());
            }
        });
    }

    /**
     * Asynchronously creates a new task and persists it via the DAO.
     * On success, updates the in-memory lists and notifies observers.
     *
     * @param title       the task title; must not be empty
     * @param description the task description; may be empty
     * @param priority    the task priority; must not be null
     */
    public void addTask(String title, String description, TaskPriority priority) {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                System.out.println("Attempting to add task: " + title + "\nDesc: " + description);
                ITask newTask = new Task(0,title, description, new ToDoState(),new Date(), priority);
                tasksDAO.addTask(newTask);
                this.allTasks.add(newTask);
                this.tasks = new ArrayList<>(this.allTasks);
                notifyObservers();
            } catch (TasksDAOException e) {
                System.err.println("Error adding task: " + e.getMessage());
            }
        });
    }

    /**
     * Convenience handler that validates input and delegates to {@link #addTask(String, String, TaskPriority)}.
     *
     * @param title       the task title; must not be empty
     * @param description the task description; may be empty
     * @param priority    the task priority; must not be null
     */
    public void addButtonPressed(String title, String description, TaskPriority priority){
        if (!title.isEmpty()) {
            addTask(title, description, priority);
        }
    }

    /**
     * Asynchronously updates an existing task identified by id and persists the change via the DAO.
     * On success, updates the in-memory lists and triggers a reload via {@link #loadTasks()}.
     *
     * @param id            the task identifier
     * @param newTitle      the new title; must not be null
     * @param newDescription the new description; must not be null
     * @param newState      the new state; must not be null
     * @param newPriority   the new priority; must not be null
     */
    public void updateTask(int id, String newTitle, String newDescription, TaskState newState, TaskPriority newPriority) {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                System.out.println("Attempting to update task ID: " + id);
                Task task = (Task) tasksDAO.getTask(id);
                if (task == null) {
                    System.err.println("Task not found for update. ID: " + id);
                    return;
                }

                task.setTitle(newTitle);
                task.setDescription(newDescription);
                task.setState(newState);
                task.setPriority(newPriority);

                //Updating the task on DB and in memory by searching for it using its id.
                tasksDAO.updateTask(task);
                allTasks.replaceAll(t -> t.getId() == id ? task : t);
                loadTasks();

            } catch (TasksDAOException e) {
                System.err.println("Error updating task: " + e.getMessage());
            }
        });
    }

    /**
     * Convenience handler that delegates to {@link #updateTask(int, String, String, TaskState, TaskPriority)}.
     */
    public void updateButtonPressed(int id, String newTitle, String newDescription, TaskState newState,TaskPriority newPriority) {
        updateTask(id, newTitle, newDescription, newState, newPriority);
    }

    /**
     * Asynchronously advances the state of the specified task to the next state,
     * persists the change, updates in-memory lists, and notifies observers.
     *
     * @param taskId the task identifier
     */
    public void moveTaskStateUp(int taskId) {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                Task task = (Task) tasksDAO.getTask(taskId);
                if (task == null) return;

                task.setState(task.getState().next());
                tasksDAO.updateTask(task);

                allTasks.replaceAll(t -> t.getId() == taskId ? task : t);
                tasks.replaceAll(t -> t.getId() == taskId ? task : t);
                notifyObservers();
            } catch (TasksDAOException e) {
                System.err.println("Error updating task state: " + e.getMessage());
            }
        });
    }

    /**
     * Convenience handler that delegates to {@link #moveTaskStateUp(int)}.
     *
     * @param taskId the task identifier
     */
    public void upButtonPressed(int taskId) {
        moveTaskStateUp(taskId);
    }

    /**
     * Asynchronously moves the state of the specified task to the previous state,
     * persists the change, updates in-memory lists, and notifies observers.
     *
     * @param taskId the task identifier
     */
    public void moveTaskStateDown(int taskId) {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                Task task = (Task) tasksDAO.getTask(taskId);
                if (task == null) return;

                task.setState(task.getState().previous());
                tasksDAO.updateTask(task);

                allTasks.replaceAll(t -> t.getId() == taskId ? task : t);
                tasks.replaceAll(t -> t.getId() == taskId ? task : t);
                notifyObservers();
            } catch (TasksDAOException e) {
                System.err.println("Error updating task state: " + e.getMessage());
            }
        });
    }

    /**
     * Convenience handler that delegates to {@link #moveTaskStateDown(int)}.
     *
     * @param taskId the task identifier
     */
    public void downButtonPressed(int taskId) {
        moveTaskStateDown(taskId);
    }

    /**
     * Asynchronously deletes the specified task from the DAO and in-memory lists, then notifies observers.
     *
     * @param id the task identifier
     */
    public void deleteTask(int id) {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                tasksDAO.deleteTask(id);
                this.allTasks.removeIf(task -> task.getId() == id);
                this.tasks.removeIf(task -> task.getId() == id);
                notifyObservers();
            } catch (TasksDAOException e) {
                System.err.println("Error deleting task: " + e.getMessage());
            }
        });
    }

    /**
     * Convenience handler that delegates to {@link #deleteTask(int)}.
     *
     * @param id the task identifier
     */
    public void deleteButtonPressed(int id) {
        deleteTask(id);
    }

    /**
     * Asynchronously deletes all tasks via the DAO, clears in-memory lists, and notifies observers.
     */
    public void deleteAllTasks() {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                tasksDAO.deleteTasks();
                this.allTasks.clear();
                this.tasks.clear();
                notifyObservers();
            } catch (TasksDAOException e) {
                System.err.println("Error deleting tasks: " + e.getMessage());
            }
        });
    }

    /**
     * Asynchronously generates a report of all tasks.
     * Uses {@link ReportVisitor} to collect data and an {@link IReportExporter} chosen by the format key.
     *
     * <p>Supported format keys are those present in {@link #exporters}, such as "Terminal", "PDF", "CSV", "JSON".</p>
     * <p>For file-based exporters, the output path is "report.&lt;format-lowercase&gt;".</p>
     *
     * @param format the exporter key (e.g., "PDF"); case-sensitive
     */
    public void generateReport(String format) {
        getService().submit(() -> {
            // 1. Use the Visitor to gather data
            ReportVisitor visitor = new ReportVisitor();
            allTasks.forEach(task -> task.accept(visitor));
            ReportData reportData = visitor.getReport();

            IReportExporter exporter = exporters.get(format);
            // 2. Use the Adapter to export the report
            if (exporter != null) {
                exporter.export(reportData, "report." + format.toLowerCase()); // The path can be dynamic
            } else {
                System.err.println("Unsupported report format: " + format);
            }
        });
    }

    /**
     * Applies filtering to the full task list and updates the visible list, then sorts and notifies observers.
     *
     * <p>Rules:</p>
     * <ul>
     *   <li>state: if not "All" (case-insensitive), filters by task state.</li>
     *   <li>titleTerm: substring match if non-empty.</li>
     *   <li>descriptionTerm: substring match if non-empty.</li>
     *   <li>If both titleTerm and descriptionTerm are provided, both must match.</li>
     *   <li>idTerm: if parseable as an integer, filters by exact id; invalid numbers are ignored.</li>
     * </ul>
     *
     * <p>After filtering, the current {@link SortingStrategy} is applied.</p>
     *
     * @param state            the desired state name or "All"
     * @param titleTerm        a search term for titles; may be null/empty
     * @param descriptionTerm  a search term for descriptions; may be null/empty
     * @param idTerm           an integer id as string; may be null/empty
     */
    public void filterTasks(String state, String titleTerm, String descriptionTerm, String idTerm) {
        if (allTasks == null) {
            return;
        }

        TaskFilter combinedFilter = tasks -> tasks;

        if (!"All".equalsIgnoreCase(state)) {
            combinedFilter = combinedFilter.and(TaskFilter.byState(state));
        }

        boolean hasTitleSearch = titleTerm != null && !titleTerm.trim().isEmpty();
        boolean hasDescriptionSearch = descriptionTerm != null && !descriptionTerm.trim().isEmpty();

        if (hasTitleSearch && hasDescriptionSearch) {
            TaskFilter titleAndDescriptionFilter = TaskFilter.byTitle(titleTerm).and(TaskFilter.byDescription(descriptionTerm));
            combinedFilter = combinedFilter.and(titleAndDescriptionFilter);
        } else if (hasTitleSearch) {
            combinedFilter = combinedFilter.and(TaskFilter.byTitle(titleTerm));
        } else if (hasDescriptionSearch) {
            combinedFilter = combinedFilter.and(TaskFilter.byDescription(descriptionTerm));
        }

        if (idTerm != null && !idTerm.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idTerm);
                combinedFilter = combinedFilter.and(TaskFilter.byId(id));
            } catch (NumberFormatException e) {
                // Ignoring illegal input
            }
        }

        this.tasks = combinedFilter.filter(this.allTasks);
        sortTasks();
        notifyObservers();
    }

    /**
     * Returns the current visible list of tasks (after filtering and sorting).
     * The returned list is mutable; callers must not modify it.
     *
     * @return the current visible tasks list; never null
     */
    public List<ITask> getTasks() {
        return tasks;
    }

    /**
     * Sets the DAO used by this ViewModel.
     * Prefer {@link #setModel(ITasksDAO)} for interface consistency.
     *
     * @param tasksDAO the DAO to use; must not be null
     */
    public void setTasksDAO(ITasksDAO tasksDAO) { 
        this.tasksDAO = tasksDAO;
    }

    /**
     * Returns the DAO used by this ViewModel.
     * Prefer {@link #getModel()} for interface consistency.
     *
     * @return the DAO; may be null if not set
     */
    public ITasksDAO getTasksDAO() {
        return tasksDAO;
    }

    /**
     * Returns the executor service used for background work.
     * Exposed primarily for testing and controlled shutdown.
     *
     * @return the executor service; never null
     */
    public ExecutorService getService() {return service;}

    /**
     * Initiates an orderly shutdown of the background executor, waiting up to 60 seconds
     * for tasks to complete. If tasks do not complete in time, a forced shutdown is attempted.
     */
    public void shutdown() {
        service.shutdown();
        try {
            if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
        }
    }
}
