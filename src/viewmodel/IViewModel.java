package viewmodel;

import model.dao.ITasksDAO;
import view.IView;
import view.TasksObserver;

public interface IViewModel {
    void addObserver(TasksObserver observer);
    void removeObserver(TasksObserver observer);
    void notifyObservers();
    void setView(IView view);
    void setModel(ITasksDAO tasksDAO);
    IView getView();
    ITasksDAO getModel();
}
