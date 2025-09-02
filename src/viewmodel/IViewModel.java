package viewmodel;

import model.dao.ITasksDAO;
import view.IView;
import view.TasksObserver;

public interface IViewModel {
    public void addObserver(TasksObserver observer);
    public void removeObserver(TasksObserver observer);
    public void notifyObservers();
    public void setView(IView view);
    public void setModel(ITasksDAO tasksDAO);
    public IView getView();
    public ITasksDAO getModel();
}
