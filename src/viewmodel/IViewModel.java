package viewmodel;

import model.dao.ITasksDAO;
import view.IView;
import view.TasksObserver;

/**
 * MVVM ViewModel contract.
 * <p>
 * Coordinates between the UI-facing {@link IView} and the data layer ({@link ITasksDAO}),
 * exposes observer registration for view-related updates, and provides lifecycle hooks
 * for wiring the view and the model.
 *
 * <h3>Typical lifecycle</h3>
 * <ol>
 *   <li>Create a ViewModel implementation.</li>
 *   <li>Provide the model via {@link #setModel(ITasksDAO)}.</li>
 *   <li>Attach the view via {@link #setView(IView)}.</li>
 *   <li>Register observers with {@link #addObserver(TasksObserver)} as needed.</li>
 *   <li>Invoke ViewModel operations; call {@link #notifyObservers()} after state changes.</li>
 * </ol>
 *
 * <h3>Threading</h3>
 * Unless otherwise documented by an implementation, methods are expected to be called
 * from the UI thread; observer callbacks are invoked on the thread that calls
 * {@link #notifyObservers()}.
 *
 * <h3>Nullability</h3>
 * Parameters marked “must not be null” will result in undefined behavior if null is passed.
 */
public interface IViewModel {
<<<<<<

    <HEAD
    void addObserver(TasksObserver observer);

    void removeObserver(TasksObserver observer);

    void notifyObservers();

    void setView(IView view);

    void setModel(ITasksDAO tasksDAO);

    IView getView();

    ITasksDAO getModel();
}
