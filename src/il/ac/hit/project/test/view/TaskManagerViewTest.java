package il.ac.hit.project.test.view;

import il.ac.hit.project.main.model.task.TaskState;
import il.ac.hit.project.main.model.task.ToDoState;
import il.ac.hit.project.main.view.TaskManagerView;

import javax.swing.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link TaskManagerView} class.
 * <p>
 * This class specifically tests the behavior of the {@code selectTaskStateInComboBox} method
 * to ensure that the correct state is selected within the combo box.
 */
class TaskManagerViewTest {

    /**
     * Tests that the combo box correctly selects the first state (ToDoState).
     */
    @Test
    void testSelectTaskStateInComboBox_selectFirstState() {
        // Arrange
        TaskState state1 = new ToDoState();
        TaskState state2 = new ToDoState().next();
        TaskState state3 = new ToDoState().next().next();

        TaskManagerView taskManagerView = new TaskManagerView();
        JComboBox<TaskState> taskStateComboBox = taskManagerView.getTaskStateComboBox();

        taskStateComboBox.addItem(state1);
        taskStateComboBox.addItem(state2);
        taskStateComboBox.addItem(state3);

        // Act
        taskManagerView.selectTaskStateInComboBox(state1);

        // Assert
        assertEquals(state1, taskStateComboBox.getSelectedItem());
    }

    /**
     * Tests that the combo box correctly selects the second state (InProgressState).
     */
    @Test
    void testSelectTaskStateInComboBox_selectSecondState() {
        // Arrange
        TaskState state1 = new ToDoState();
        TaskState state2 = new ToDoState().next();
        TaskState state3 = new ToDoState().next().next();

        TaskManagerView taskManagerView = new TaskManagerView();
        JComboBox<TaskState> taskStateComboBox = taskManagerView.getTaskStateComboBox();

        taskStateComboBox.addItem(state1);
        taskStateComboBox.addItem(state2);
        taskStateComboBox.addItem(state3);

        // Act
        taskManagerView.selectTaskStateInComboBox(state2);

        // Assert
        assertEquals(state2, taskStateComboBox.getSelectedItem());
    }

    /**
     * Tests that the combo box correctly selects the third state (CompletedState).
     */
    @Test
    void testSelectTaskStateInComboBox_selectThirdState() {
        // Arrange
        TaskState state1 = new ToDoState();
        TaskState state2 = new ToDoState().next();
        TaskState state3 = new ToDoState().next().next();

        TaskManagerView taskManagerView = new TaskManagerView();
        JComboBox<TaskState> taskStateComboBox = taskManagerView.getTaskStateComboBox();

        taskStateComboBox.addItem(state1);
        taskStateComboBox.addItem(state2);
        taskStateComboBox.addItem(state3);

        // Act
        taskManagerView.selectTaskStateInComboBox(state3);

        // Assert
        assertEquals(state3, taskStateComboBox.getSelectedItem());
    }

    /**
     * Tests that the combo box correctly defaults to the first item when the provided state is not found.
     */
    @Test
    void testSelectTaskStateInComboBox_stateNotFound() {
        // Arrange
        TaskState state1 = new ToDoState();
        TaskState state2 = new ToDoState().next();
        TaskState state3 = new ToDoState().next().next();
        TaskState notInComboBoxState = new ToDoState();

        TaskManagerView taskManagerView = new TaskManagerView();
        JComboBox<TaskState> taskStateComboBox = taskManagerView.getTaskStateComboBox();

        taskStateComboBox.addItem(state1);
        taskStateComboBox.addItem(state2);
        taskStateComboBox.addItem(state3);

        // Act
        taskManagerView.selectTaskStateInComboBox(notInComboBoxState);

        // Assert
        assertEquals(state1, taskStateComboBox.getSelectedItem());
    }
}