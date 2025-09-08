package viewmodel;

import model.dao.ITasksDAO;
import model.dao.TasksDAOException;
import model.report.*;
import model.task.ITask;
import model.task.Task;
import model.task.TaskState;
import model.task.ToDoState;
import view.MessageType;
import view.ObservableProperty.IObservableCollection;
import view.ObservableProperty.IPropertyObserver;
import view.ObservableProperty.IObservableProperty;
import view.ObservableProperty.ObservableCollection;
import view.ObservableProperty.ObservableProperty;
import view.TaskManagerView;
import view.IView;
import viewmodel.combinator.TaskFilter;
import viewmodel.strategy.SortByIDStrategy;
import viewmodel.strategy.ISortingStrategy;


import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static view.MessageType.ERROR;

/**
 * ViewModel that coordinates between the View and the Tasks data layer in an MVVM architecture.
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Asynchronously load, add, update, and delete tasks using {@link ITasksDAO}.</li>
 *   <li>Maintain an in-memory collection of all tasks and a working list that can be filtered and sorted.</li>
 *   <li>Apply sorting via pluggable {@link ISortingStrategy} implementations.</li>
 *   <li>Filter tasks using combinable predicates from {@link TaskFilter}.</li>
 *   <li>Generate reports using a Visitor + Adapter approach ({@link ReportVisitor}, {@link IReportExporter}).</li>
 * </ul>
 *
 * <h3>Threading</h3>
 * <ul>
 *   <li>DAO interactions are executed on a background {@link ExecutorService}.</li>
 *   <li>{@link IObservableProperty#notifyListeners()} is called from the thread that performs the change; in this class
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
 * @see ISortingStrategy
 * @see TaskFilter
 */
public class TasksViewModel implements IViewModel {

    // The currently attached view. Maybe null until explicitly set.
    private IView view;

    // The data-access object used to persist and retrieve tasks.
    private ITasksDAO tasksDAO;

    // The current working list of tasks, after filtering and sorting.
    private List<ITask> tasks = new ArrayList<>();

    // The complete in-memory set of tasks last loaded from the DAO.
    private List<ITask> allTasks = new ArrayList<>();

    // Registry of report exporters keyed by human-readable format (e.g., "PDF", "CSV").
    private final Map<String, IReportExporter> exporters = new HashMap<>();

    // The active sorting strategy used to order {@link #tasks}.
    private ISortingStrategy currentSortingStrategy;


    // Executor used to perform DAO operations and other background work.
    private final ExecutorService service;
    /*
     Viewport refresh actions to implement:
     -add task -> new list item -> list updates | DONE
     -update task -> list updates -> single cell updates | DONE
     -select list item -> form fills, buttons activate | DONE
     -press enter upon new value in filter fields -> list updates
     #MAKE SURE -> no task selected -> delete buttons turn off
     -update creation date label to show "now" on no task selected
     */

    //Selected task data-bound
    private final IObservableProperty<ITask> selectedTask = new ObservableProperty<>(null);

    //Task list data-bound
    private final IObservableCollection<ITask> tasksList = new ObservableCollection<>();

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
        setSortingStrategy(new SortByIDStrategy());
        this.service = Executors.newFixedThreadPool(8);
        setPropertyListeners();
        loadTasks(); // Initial load
    }

    public TasksViewModel(ITasksDAO dao, IView view, ExecutorService executorService) {
        setModel(dao);
        setView(view);
        this.service = executorService;
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
    public void setSortingStrategy(ISortingStrategy strategy) {
        this.currentSortingStrategy = strategy;
        sortTasks(); // Re-sort the current list of tasks
        getTasksList().notifyListeners();
    }

    /**
     * Applies the current sorting strategy to the visible task list, if any.
     * No-op if there is no strategy or the list is empty.
     */
    private void sortTasks() {
        if (getCurrentSortingStrat() != null && !getTasksList().get().isEmpty()) {
            getCurrentSortingStrat().sort(getTasksList().get());
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
                ITask[] tasksArray = getModel().getTasks();
                // Convert the array to an ArrayList for mutable operations
                setAllTasks( new ArrayList<>(Arrays.asList(tasksArray)));
                //Use the observer to update the list in the UI
                getTasksList().setValue(new ArrayList<>(getAllTasks()));
                getView().setTasks(Arrays.asList(tasksArray));
                System.out.println( "Task List:" +getTasksList().toString());
            } catch (TasksDAOException e){
                System.err.println("Error loading tasks: " + e.getMessage() + (e.getCause() != null ? "\nCause: " + e.getCause() : ""));
                getView().showMessage("Error loading tasks: " + e.getMessage(),ERROR);
            }
        });
    }

    /**
     * Asynchronously creates a new task and persists it via the DAO.
     * On success, updates the in-memory lists and notifies observers.
     *
     * @param title       the task title; must not be empty
     * @param description the task description; may be empty
     */
    public void addTask(String title, String description) {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                System.out.println("Attempting to add task: " + title + "\nDesc: " + description);
                ITask newTask = new Task(0,title, description, new ToDoState());
                getModel().addTask(newTask);
                getAllTasks().add(newTask);
                getTasksList().appendValue(newTask);
                // Success message: Operation completed successfully.
                getView().showMessage("Task \"" + title + "\" added successfully!", MessageType.SUCCESS);
            } catch (TasksDAOException e) {
                System.err.println("Error adding task: " + e.getMessage());
                // Error message: The operation failed.
                getView().showMessage("Error adding task: " + e.getMessage(), MessageType.ERROR);
            }
        });
    }

    /**
     * Convenience handler that validates input and delegates to {@link #addTask(String, String)}.
     *
     * @param title       the task title; must not be empty
     * @param description the task description; may be empty
     */
    public void addButtonPressed(String title, String description){
        if (!title.isEmpty()) {
            addTask(title, description);
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
     */
    public void updateTask(int id, String newTitle, String newDescription, TaskState newState) {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                System.out.println("Attempting to update task ID: " + id);
                Task task = (Task) getModel().getTask(id);
                if (task == null) {
                    System.err.println("Task not found for update. ID: " + id);
                    // Error message: Task not found
                    getView().showMessage("Error updating task: Task not found.", MessageType.ERROR);
                    return;
                }

                task.setTitle(newTitle);
                task.setDescription(newDescription);
                task.setState(newState);

                //Updating the task on DB and in memory by searching for it using its id.
                getModel().updateTask(task);
                getAllTasks().replaceAll(t -> t.getId() == id ? task : t);
                loadTasks();
                // Success message: Task updated successfully
                getView().showMessage("Task \"" + newTitle + "\" updated successfully!", MessageType.SUCCESS);

            } catch (TasksDAOException e) {
                System.err.println("Error updating task: " + e.getMessage());
                getView().showMessage("Error updating task: " + e.getMessage(), MessageType.ERROR);
            }
        });
    }

    /**
     * Convenience handler that delegates to {@link #updateTask(int, String, String, TaskState)}.
     */
    public void updateButtonPressed(int id, String newTitle, String newDescription, TaskState newState) {
        updateTask(id, newTitle, newDescription, newState);
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
                Task task = (Task) getModel().getTask(taskId);
                if (task == null) {
                    getView().showMessage("Error: Task with ID " + taskId + " not found.", MessageType.ERROR);
                    return;
                }

                task.setState(task.getState().next());
                getModel().updateTask(task);

                getAllTasks().replaceAll(t -> t.getId() == taskId ? task : t);
                getTasksList().get().replaceAll(t -> t.getId() == taskId ? task : t);
                getTasksList().notifyListeners();
                getView().showMessage("Task state updated to " + task.getState().getDisplayName() + ".", MessageType.SUCCESS);
            } catch (TasksDAOException e) {
                System.err.println("Error updating task state: " + e.getMessage());
                getView().showMessage("Error updating task state: " + e.getMessage(), MessageType.ERROR);
            }
        });
    }

    /**
     * Convenience handler that delegates to {@link #moveTaskStateUp(int)}.
     */
    public void upButtonPressed() {
        moveTaskStateUp(getSelectedTask().get().getId());
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
                Task task = (Task) getModel().getTask(taskId);
                if (task == null) {
                    getView().showMessage("Error: Task with ID " + taskId + " not found.", MessageType.ERROR);
                    return;
                }

                task.setState(task.getState().previous());
                getModel().updateTask(task);

                getAllTasks().replaceAll(t -> t.getId() == taskId ? task : t);
                getTasksList().get().replaceAll(t -> t.getId() == taskId ? task : t);
                getTasksList().notifyListeners();
                getView().showMessage("Task state updated to " + task.getState().getDisplayName() + ".", MessageType.SUCCESS);
            } catch (TasksDAOException e) {
                System.err.println("Error updating task state: " + e.getMessage());
                getView().showMessage("Error updating task state: " + e.getMessage(), MessageType.ERROR);
            }
        });
    }

    /**
     * Convenience handler that delegates to {@link #moveTaskStateDown(int)}.
     */
    public void downButtonPressed() {
        moveTaskStateDown(getSelectedTask().get().getId());
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
                getModel().deleteTask(id);
                getAllTasks().removeIf(task -> task.getId() == id);
                getTasksList().get().removeIf(task -> task.getId() == id);
                getTasksList().notifyListeners();
                //TODO: check if statement is useless
                getTasks().removeIf(task -> task.getId() == id);
                getView().showMessage("Task with ID " + id + " deleted successfully.", MessageType.SUCCESS);
            } catch (TasksDAOException e) {
                System.err.println("Error deleting task: " + e.getMessage());
                getView().showMessage("Error deleting task: " + e.getMessage(), MessageType.ERROR);
            }
        });
    }

    /**
     * Convenience handler that delegates to {@link #deleteTask(int)}.
     */
    public void deleteButtonPressed() {
        deleteTask(getSelectedTask().get().getId());
    }

    /**
     * Asynchronously deletes all tasks via the DAO, clears in-memory lists, and notifies observers.
     */
    public void deleteAllTasks() {
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                getModel().deleteTasks();
                getAllTasks().clear();
                getTasks().clear();
                getTasksList().get().clear();
                getTasksList().notifyListeners();
                getView().showMessage("All tasks deleted successfully.", MessageType.SUCCESS);
            } catch (TasksDAOException e) {
                System.err.println("Error deleting tasks: " + e.getMessage());
                getView().showMessage("Error deleting all tasks: " + e.getMessage(), MessageType.ERROR);
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
        //Wrap DB calls with our service executor
        getService().submit(() -> {
            try {
                ReportVisitor visitor = new ReportVisitor();
                getAllTasks().forEach(task -> {
                    if (task instanceof Task t) {
                        visitor.visit(t);
                    }
                });

                ReportData reportData = visitor.getReport();

                IReportExporter exporter = exporters.get(format);

                if (exporter != null) {
                    String fileName = "report." + format.toLowerCase();
                    exporter.export(reportData, fileName);
                    getView().showMessage("Report generated successfully: " + fileName, MessageType.SUCCESS);
                } else {
                    System.err.println("Unsupported report format: " + format);
                    getView().showMessage("Unsupported report format: " + format, MessageType.WARNING);
                }
            } catch (Exception e) {
                System.err.println("Error generating report: " + e.getMessage());
                getView().showMessage("Error generating report: " + e.getMessage(), MessageType.ERROR);
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
     * <p>After filtering, the current {@link ISortingStrategy} is applied.</p>
     *
     * @param state            the desired state name or "All"
     * @param titleTerm        a search term for titles; may be null/empty
     * @param descriptionTerm  a search term for descriptions; may be null/empty
     * @param idTerm           an integer id as string; may be null/empty
     */
    public void filterTasks(String state, String titleTerm, String descriptionTerm, String idTerm) {
        //If we have no tasks on DB
        if (getAllTasks() == null) {
            return;
        }

        //Compose combinator filter
        TaskFilter combinedFilter = tasks -> tasks;

        //Case: state filter selected.
        if (!"All".equalsIgnoreCase(state)) {
            combinedFilter = combinedFilter.and(TaskFilter.byState(state));
        }

        //Validating missing values
        boolean hasTitleSearch = titleTerm != null && !titleTerm.trim().isEmpty();
        boolean hasDescriptionSearch = descriptionTerm != null && !descriptionTerm.trim().isEmpty();

        if (hasTitleSearch && hasDescriptionSearch) {
            //By Title & Description
            TaskFilter titleAndDescriptionFilter = TaskFilter.byTitle(titleTerm).and(TaskFilter.byDescription(descriptionTerm));
            combinedFilter = combinedFilter.and(titleAndDescriptionFilter);
        } else if (hasTitleSearch) {
            //By Title
            combinedFilter = combinedFilter.and(TaskFilter.byTitle(titleTerm));
        } else if (hasDescriptionSearch) {
            //By Description
            combinedFilter = combinedFilter.and(TaskFilter.byDescription(descriptionTerm));
        }

        //By specific ID
        if (idTerm != null && !idTerm.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idTerm);
                combinedFilter = combinedFilter.and(TaskFilter.byId(id));
            } catch (NumberFormatException e) {
                // Ignoring illegal input
            }
        }

        getTasksList().setValue(combinedFilter.filter(getAllTasks()));
        sortTasks();
        getTasksList().notifyListeners();
        //notifyObservers();
    }

    public void setPropertyListeners(){
        //Selected Task UI update
        selectedTask.addListener(new IPropertyObserver<ITask>() {
            @Override
            public void update(ITask value) {
                System.out.println("Updating the form with selected task..");
                getView().setFormData(value);
            }
        });

        tasksList.addListener(new IPropertyObserver<List<ITask>>() {
            @Override
            public void update(List<ITask> value) {
                System.out.println("Updating task list");
                getView().setTasks(value);
            }
        });
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
     * Sets the task list.
     *
     * @param tasks the task list to use
     */
    public void setTasks(List<ITask> tasks) {
        this.tasks = tasks;
    }

    /**
     * Returns the list of all tasks available on memory
     *
     * @return the allTasks lists; never null
     */
    public List<ITask> getAllTasks() {
        return allTasks;
    }

    /**
     * Sets the allTask list.
     *
     * @param allTasks the task list to use
     */
    public void setAllTasks(List<ITask> allTasks) {
        this.allTasks = allTasks;
    }

    public IObservableProperty<ITask> getSelectedTask() {
        return selectedTask;
    }

    public IObservableCollection<ITask> getTasksList() {
        return tasksList;
    }

    /**
     * Returns the current set sorting strategy
     *
     * @return the current sorting strategy.
     */
    public ISortingStrategy getCurrentSortingStrat(){
        return currentSortingStrategy;
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
        //Shutdown
        service.shutdown();
        try {
            if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            //Brute force shutdown
            service.shutdownNow();
        }
    }
}