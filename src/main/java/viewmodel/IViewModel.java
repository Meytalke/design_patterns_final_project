package il.ac.hit.project.main.viewmodel;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.view.IView;
import il.ac.hit.project.main.view.ObservableProperty.IObservableProperty;
import il.ac.hit.project.main.view.ObservableProperty.IPropertyObserver;


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
 *   <li>Register observers with {@link view.ObservableProperty.IObservableProperty#addListener(IPropertyObserver)} as needed.</li>
 *   <li>Invoke ViewModel operations; call {@link IObservableProperty#notifyListeners()} after state changes.</li>
 * </ol>
 *
 * <h3>Threading</h3>
 * Unless otherwise documented by an implementation, methods are expected to be called
 * from the UI thread; observer callbacks are invoked on the thread that calls
 * {@link IObservableProperty#notifyListeners()}.
 *
 * <h3>Nullability</h3>
 * Parameters marked “must not be null” will result in undefined behavior if null is passed.
 */
public interface IViewModel {
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