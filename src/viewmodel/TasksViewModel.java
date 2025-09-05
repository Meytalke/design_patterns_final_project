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


public class TasksViewModel implements IViewModel {

    private IView view;
    private ITasksDAO tasksDAO;
    //This list will contain observers to UI components that need to be updated
    // to start, we will update the whole UI everytime the list of tasks changes
    private final List<TasksObserver> observers = new ArrayList<>();
    private List<ITask> tasks = new ArrayList<>();
    private List<ITask> allTasks = new ArrayList<>();
    private final Map<String, IReportExporter> exporters = new HashMap<>();
    private SortingStrategy currentSortingStrategy;
    private final ExecutorService service;
    //private ObservableProperty<List<ITask>> listObservers = new ObservableProperty<>(tasks);


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

    public void addObserver(TasksObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TasksObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (TasksObserver observer : observers) {
            observer.onTasksChanged(tasks);
        }
    }

    // A public method to allow the view to change the sorting strategy
    public void setSortingStrategy(SortingStrategy strategy) {
        this.currentSortingStrategy = strategy;
        sortTasks(); // Re-sort the current list of tasks
        notifyObservers();
    }

    // A private helper method to apply the current sorting strategy
    private void sortTasks() {
        if (currentSortingStrategy != null && !tasks.isEmpty()) {
            currentSortingStrategy.sort(this.tasks);
        }
    }

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

    public void addButtonPressed(String title, String description, TaskPriority priority){
        if (!title.isEmpty()) {
            addTask(title, description, priority);
        }
    }

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

    public void updateButtonPressed(int id, String newTitle, String newDescription, TaskState newState,TaskPriority newPriority) {
        updateTask(id, newTitle, newDescription, newState, newPriority);
    }

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

    public void upButtonPressed(int taskId) {
        moveTaskStateUp(taskId);
    }

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

    public void downButtonPressed(int taskId) {
        moveTaskStateDown(taskId);
    }


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

    public void deleteButtonPressed(int id) {
        deleteTask(id);
    }

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

    public List<ITask> getTasks() {
        return tasks;
    }
    
    public void setTasksDAO(ITasksDAO tasksDAO) { 
        this.tasksDAO = tasksDAO;
    }
    
    public ITasksDAO getTasksDAO() {
        return tasksDAO;
    }
    
    @Override
    public void setView(IView view){
        this.view = view;
    }

    @Override
    public void setModel(ITasksDAO tasksDAO) {
        this.tasksDAO = tasksDAO;
    }

    @Override
    public IView getView() {
        return view;
    }

    @Override
    public ITasksDAO getModel() {
        return tasksDAO;
    }

    public ExecutorService getService() {return service;}

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
