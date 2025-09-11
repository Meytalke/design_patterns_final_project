package il.ac.hit.project.test.view;

import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.Task;
import il.ac.hit.project.main.model.task.TaskState;
import il.ac.hit.project.main.model.task.ToDoState;
import il.ac.hit.project.main.view.ObservableProperty.IObservableProperty;
import il.ac.hit.project.main.view.ObservableProperty.ObservableProperty;
import il.ac.hit.project.main.view.TaskManagerView;
import il.ac.hit.project.main.viewmodel.TasksViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link TaskManagerView} class.
 * <p>
 * These tests verify the behavior and state management of the View component in an MVVM architecture.
 * They focus on interactions with a mocked ViewModel and proper UI state updates, rather than
 * the internal mechanics of the Swing components themselves.
 */
class TaskManagerViewTest {

    private TasksViewModel viewModel;
    private TaskManagerView view;
    private IObservableProperty<ITask> selectedTaskMock;

    /**
     * Initializes a mocked ViewModel and a new View instance before each test.
     * The View's listeners are attached by calling the `start()` method once.
     */
    @BeforeEach
    void setUp() {
        // Use a mock ViewModel to isolate the View and verify its interactions.
        viewModel = mock(TasksViewModel.class);

        // Mock the IObservableProperty that the ViewModel returns for selectedTask.
        selectedTaskMock = mock(IObservableProperty.class);

        // This is a crucial fix: configure the mock to return the mock object itself.
        // This prevents NullPointerExceptions when the View tries to call get() on it.
        when(viewModel.getSelectedTask()).thenReturn(selectedTaskMock);

        // Create the View instance and set the mocked ViewModel.
        view = new TaskManagerView();
        view.setViewModel(viewModel);

        // This is the main fix: call start() only once here.
        // This ensures all listeners are set up, but no duplicates.
        view.start();

    }

    /**
     * Tests that `setFormData` correctly populates the UI fields and enables the appropriate
     * buttons when a valid task is provided. This simulates a user selecting a task from the list.
     */
    @Test
    void testSetFormData_withValidTask_updatesUIAndEnablesButtons() {
        // Arrange
        ITask task = new Task(1, "Test Task", "Test Description", new ToDoState().next());

        // Act
        view.setFormData(task);

        // Assert
        assertEquals("Test Task", view.getTaskTitleInputF().getText());
        assertEquals("Test Description", view.getDescriptionInputTA().getText());

        assertFalse(view.getAddButton().isEnabled());
        assertTrue(view.getUpdateButton().isEnabled());
        assertTrue(view.getDeleteButton().isEnabled());
        assertTrue(view.getUpButton().isEnabled());
        assertTrue(view.getDownButton().isEnabled());
        assertTrue(view.getDeselectButton().isEnabled());
    }

    /**
     * Tests that `setFormData` correctly resets the form when a null task is provided.
     * This simulates clearing the selection in the task list.
     */
    @Test
    void testSetFormData_withNullTask_resetsForm() {
        // Arrange
        view.getTaskTitleInputF().setText("Filled Title");
        view.getUpdateButton().setEnabled(true);

        // Act
        view.setFormData(null);

        // Assert
        assertEquals("", view.getTaskTitleInputF().getText());
        assertTrue(view.getAddButton().isEnabled());
        assertFalse(view.getUpdateButton().isEnabled());
        assertFalse(view.getUpButton().isEnabled());
        assertFalse(view.getDownButton().isEnabled());
        assertFalse(view.getDeselectButton().isEnabled());
    }

    /**
     * Tests that a button click on the 'Add' button correctly delegates the action to the
     * ViewModel with the correct data from the UI fields.
     */
    @Test
    void testAddButton_delegatesToViewModelAndResetsForm() {
        // Arrange
        view.getTaskTitleInputF().setText("New Task");
        view.getDescriptionInputTA().setText("New Task Description");

        // Act
        view.getAddButton().doClick();

        // Assert
        verify(viewModel).addButtonPressed(eq("New Task"), eq("New Task Description"));
        assertEquals("", view.getTaskTitleInputF().getText());
    }

    /**
     * Tests that the `setTasks` method correctly updates the underlying `DefaultListModel`
     * of the task list, which causes the JList to refresh.
     */
    @Test
    void testSetTasks_updatesListModelCorrectly() {
        // Arrange
        ITask task1 = new Task(1, "Task One", "Desc 1", new ToDoState());
        ITask task2 = new Task(2, "Task Two", "Desc 2", new ToDoState().next());
        List<ITask> tasks = Arrays.asList(task1, task2);

        // Act
        view.setTasks(tasks);

        // Assert
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<ITask> listModel = view.getListModel();
            assertEquals(2, listModel.size());
            assertEquals(task1, listModel.getElementAt(0));
            assertEquals(task2, listModel.getElementAt(1));
        });
    }

    /**
     * Tests that selecting an item in the `JList` correctly updates the ViewModel's
     * selected task. This simulates the user clicking a task in the UI.
     */
    @Test
    void testTaskListSelection_updatesSelectedTaskInViewModel() {
        // Arrange
        ITask task = new Task(1, "Selected Task", "Description", new ToDoState());

        // Add the task to the JList's model.
        DefaultListModel<ITask> listModel = view.getListModel();
        listModel.addElement(task);

        // Act
        // Simulate the user selecting the first item in the JList. This action triggers the listener.
        view.getTaskList().setSelectedIndex(0);

        // Assert
        ArgumentCaptor<ITask> taskCaptor = ArgumentCaptor.forClass(ITask.class);
        verify(selectedTaskMock).setValue(taskCaptor.capture());
        assertEquals(task, taskCaptor.getValue());
    }

    /**
     * Tests that the `selectTaskStateInComboBox` method correctly sets the selected item
     * based on the provided `TaskState` class.
     */
    @Test
    void testSelectTaskStateInComboBox_selectsCorrectState() {
        // Arrange
        TaskState inProgressState = new ToDoState().next();

        // Act
        view.selectTaskStateInComboBox(inProgressState);

        // Assert
        assertEquals(inProgressState.getClass(), ((TaskState) view.getTaskStateComboBox().getSelectedItem()).getClass());
    }

    /**
     * Tests that `applyAllFilters` correctly delegates the filtering logic to the ViewModel
     * based on the input field values.
     */
    @Test
    void testApplyAllFilters_delegatesToViewModelWithCorrectParameters() {
        // Arrange
        view.getSearchTitleInput().setText("My Title");
        view.getSearchDescriptionInput().setText("My Description");
        view.getSearchIdInput().setText("123");
        view.getStateFilterComboBox().setSelectedItem("Completed");

        // Act
        // Simulate a single action event to trigger the listener attached in @BeforeEach.
        view.getSearchTitleInput().postActionEvent();

        // Assert
        verify(viewModel, times(2)).filterTasks(
                eq("Completed"),
                eq("My Title"),
                eq("My Description"),
                eq("123")
        );
    }
}