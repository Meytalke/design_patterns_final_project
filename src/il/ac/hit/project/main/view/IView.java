package il.ac.hit.project.main.view;

import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.viewmodel.IViewModel;

import java.util.List;

/**
 * UI-facing contract for a View in an MVVM architecture.
 * <p>
 * A View is responsible for rendering the user interface, binding to an {@link IViewModel},
 * and starting any required UI loop or screen presentation.
 *
 * <h3>Typical lifecycle</h3>
 * <ol>
 *   <li>Create the il.ac.hit.project.main.view instance.</li>
 *   <li>Provide a il.ac.hit.project.main.view il.ac.hit.project.main.model via {@link #setViewModel(IViewModel)}.</li>
 *   <li>Invoke {@link #start()} to display and interact with the UI.</li>
 * </ol>
 *
 * <h3>Threading</h3>
 * Implementations may require that {@code start()} be called on the UI thread of the
 * underlying toolkit (e.g., Swing/JavaFX). Consult the concrete implementation for details.
 */
public interface IView {

    /**
     * Returns the il.ac.hit.project.main.view il.ac.hit.project.main.model currently associated with this il.ac.hit.project.main.view.
     * <p>
     * Implementations may return {@code null} if a il.ac.hit.project.main.view il.ac.hit.project.main.model has not yet been assigned.
     *
     * @return the current {@link IViewModel}, or {@code null} if none is set
     */
    IViewModel getViewModel();

    /**
     * Associates a il.ac.hit.project.main.view il.ac.hit.project.main.model with this il.ac.hit.project.main.view and (re)binds UI elements as needed.
     * <p>
     * Calling this method more than once should update bindings to the new il.ac.hit.project.main.model.
     *
     * @param viewModel the il.ac.hit.project.main.view il.ac.hit.project.main.model to bind; expected to be non-null
     * @throws IllegalArgumentException if {@code viewModel} is {@code null} (implementations may enforce this)
     */
    void setViewModel(IViewModel viewModel);

    /**
     * Starts the il.ac.hit.project.main.view, creating UI bindings and presenting the interface to the user.
     * <p>
     * This method is typically called once per il.ac.hit.project.main.view instance. Later calls may
     * be ignored or may result in implementation-defined behavior.
     */
    void start();

    /**
     * Sets the list of tasks to be displayed by the il.ac.hit.project.main.view.
     *
     * This method is typically used to refresh the entire task list
     * in the UI after a change, such as adding, deleting, or updating a task.
     *
     * @param tasks the list of tasks to display; expected to be non-null
     */
    void setTasks(List<ITask> tasks);

    /**
     * Populates the il.ac.hit.project.main.view's form with data from a given task.
     *
     * This is useful for editing an existing task, where the form fields
     * need to be pre-filled with the task's current title, description, and state.
     *
     * @param task the task object containing the data to display in the form
     */
    void setFormData(ITask task);

    /**
     * Resets the UI form to its initial, empty state.
     *
     * This method should clear all input fields, radio buttons, or any
     * other form elements, making them ready for creating a new task.
     */
    void resetForm();

    /**
     * Displays a message to the user, with a specific type indicating success or failure.
     *
     * The il.ac.hit.project.main.view is responsible for determining how to render the message based on its type
     * (e.g., a green bar for success, a red box for an error).
     *
     * @param message the text of the message to display
     * @param type    the type of message (e.g., success, error, warning)
     */
    void showMessage(String message, MessageType type);
}