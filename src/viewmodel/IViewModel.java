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

    /**
     * Registers an observer to receive notifications when the ViewModel’s observable
     * state changes (e.g., task list updates, selection changes).
     *
     * <p>Implementations may ignore duplicate registrations.</p>
     *
     * @param observer the observer to add; must not be null
     */
    void addObserver(TasksObserver observer);

    /**
     * Unregisters a previously added observer.
     *
     * <p>No-op if the observer was not registered.</p>
     *
     * @param observer the observer to remove; must not be null
     */
    void removeObserver(TasksObserver observer);

    /**
     * Notifies all currently registered observers of a state change.
     *
     * <p>Implementations should strive to notify all observers even if one throws,
     * and may batch or coalesce notifications to reduce UI churn.</p>
     */
    void notifyObservers();

    /**
     * Associates the ViewModel with a view.
     *
     * <p>Setting a new view replaces the previous association. Implementations may
     * perform initial synchronization (e.g., pushing the current state to the view).</p>
     *
     * @param view the view to attach; must not be null
     */
    void setView(IView view);

    /**
     * Provides the backing data-access object.
     *
     * <p>Setting a new model replaces the previous association. Implementations should
     * not assume ownership beyond the DAO's public contract.</p>
     *
     * @param tasksDAO the model/DAO to use; must not be null
     */
    void setModel(ITasksDAO tasksDAO);

    /**
     * Returns the currently associated view, if any.
     *
     * @return the view, or null if none has been set
     */
    IView getView();

    /**
     * Returns the currently associated data-access object, if any.
     *
     * @return the model/DAO, or null if none has been set
     */
    ITasksDAO getModel();
}