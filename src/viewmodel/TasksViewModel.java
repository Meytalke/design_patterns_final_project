package viewmodel;

import model.dao.ITasksDAO;
import model.dao.TasksDAOException;
import model.report.*;
import model.task.ITask;
import model.task.Task;
import model.task.TaskState;
import view.TaskManagerView;
import view.TasksObserver;
import view.IView;
import viewmodel.combinator.TaskFilter;

import java.util.*;


public class TasksViewModel implements IViewModel {

    private IView view;
    private ITasksDAO tasksDAO;
    //This list will contain observers to UI components that need to be updated
    // to start, we will update the whole UI everytime the list of tasks changes
    private final List<TasksObserver> observers = new ArrayList<>();
    private List<ITask> tasks = new ArrayList<>();
    private List<ITask> allTasks = new ArrayList<>();
    private final Map<String, IReportExporter> exporters = new HashMap<>();


    public TasksViewModel(ITasksDAO tasksDAO, IView view) {
        this.tasksDAO = tasksDAO;
        setView(view);
        exporters.put("Terminal", new ReportAdapter());
        exporters.put("PDF", new PdfReportAdapter());
        exporters.put("CSV", new CsvReportAdapter());
        exporters.put("JSON", new JsonReportAdapter());
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

    public void loadTasks() {
        try {
            ITask[] tasksArray = tasksDAO.getTasks();
            // Convert the array to an ArrayList for mutable operations
            this.allTasks = new ArrayList<>(Arrays.asList(tasksArray));
            this.tasks = new ArrayList<>(this.allTasks);
            notifyObservers();
        } catch (TasksDAOException e){
            System.err.println("Error loading tasks: " + e.getMessage());
        }
    }

    public void addTask(String title, String description) {
        try {
            System.out.println("Attempting to add task: " + title + "\nDesc: " + description);
            ITask newTask = new Task(0,title, description,TaskState.TO_DO);
            tasksDAO.addTask(newTask);
            this.allTasks.add(newTask);
            this.tasks = new ArrayList<>(this.allTasks);
            notifyObservers();
        } catch (TasksDAOException e) {
            System.err.println("Error adding task: " + e.getMessage());
        }
    }

    public void updateTask(int id, String newTitle, String newDescription, TaskState newState) {
        try {
            ITask oldTask = tasksDAO.getTask(id);
            if (oldTask == null) {
                System.err.println("Task not found for update. ID: " + id);
                return;
            }

            ITask updatedTask = new Task(id, newTitle, newDescription, newState);
            tasksDAO.updateTask(updatedTask);
            allTasks.replaceAll(task -> task.getId() == id ? updatedTask : task);
            loadTasks();
        } catch (TasksDAOException e) {
            System.err.println("Error updating task: " + e.getMessage());
        }
    }

    public void deleteTask(int id) {
        try {
            tasksDAO.deleteTask(id);
            this.allTasks.removeIf(task -> task.getId() == id);
            this.tasks.removeIf(task -> task.getId() == id);
            notifyObservers();
        } catch (TasksDAOException e) {
            System.err.println("Error deleting task: " + e.getMessage());
        }
    }

    public void deleteAllTasks() {
        try {
            tasksDAO.deleteTasks();
            this.allTasks.clear();
            this.tasks.clear();
            notifyObservers();
        } catch (TasksDAOException e) {
            System.err.println("Error deleting tasks: " + e.getMessage());
        }
    }

    public void generateReport(String format) {
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
    public IView getView() {
        return view;
    }

    public void addButtonPressed(){
        String title = ((TaskManagerView) view).getTaskTitleInputF().getText();
        String description = ((TaskManagerView) view).getDescriptionInputTA().getText();
        if (!title.isEmpty()) {
            addTask(title, description);
            resetForm();
        }
    }
    private void resetForm() {
        ((TaskManagerView) view).getTaskTitleInputF().setText("");
        ((TaskManagerView) view).getDescriptionInputTA().setText("");
        ((TaskManagerView) view).getTaskStateComboBox().setSelectedIndex(0);
        ((TaskManagerView) view).getTaskStateComboBox().setEnabled(false);
        ((TaskManagerView) view).getAddButton().setEnabled(true);
        ((TaskManagerView) view).getUpdateButton().setEnabled(false);
        ((TaskManagerView) view).setSelectedTask(null);
        ((TaskManagerView) view).getTaskList().clearSelection();
    }

}
