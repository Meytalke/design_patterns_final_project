package view;

import model.task.ITask;
import viewmodel.IViewModel;

import java.util.List;

/**
 * UI-facing contract for a View in an MVVM architecture.
 * <p>
 * A View is responsible for rendering the user interface, binding to an {@link IViewModel},
 * and starting any required UI loop or screen presentation.
 *
 * <h3>Typical lifecycle</h3>
 * <ol>
 *   <li>Create the view instance.</li>
 *   <li>Provide a view model via {@link #setViewModel(IViewModel)}.</li>
 *   <li>Invoke {@link #start()} to display and interact with the UI.</li>
 * </ol>
 *
 * <h3>Threading</h3>
 * Implementations may require that {@code start()} be called on the UI thread of the
 * underlying toolkit (e.g., Swing/JavaFX). Consult the concrete implementation for details.
 */
public interface IView {

    /**
     * Returns the view model currently associated with this view.
     * <p>
     * Implementations may return {@code null} if a view model has not yet been assigned.
     *
     * @return the current {@link IViewModel}, or {@code null} if none is set
     */
    IViewModel getViewModel();

    /**
     * Associates a view model with this view and (re)binds UI elements as needed.
     * <p>
     * Calling this method more than once should update bindings to the new model.
     *
     * @param viewModel the view model to bind; expected to be non-null
     * @throws IllegalArgumentException if {@code viewModel} is {@code null} (implementations may enforce this)
     */
    void setViewModel(IViewModel viewModel);

    /**
     * Starts the view, creating UI bindings and presenting the interface to the user.
     * <p>
     * This method is typically called once per view instance. Later calls may
     * be ignored or may result in implementation-defined behavior.
     */
    void start();

    void setTasks(List<ITask> tasks);
    void setFormData(ITask task);
    void resetForm();
    void showMessage(String message, MessageType type);
}