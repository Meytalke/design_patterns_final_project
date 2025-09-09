package view;

import model.task.TaskState;
import model.task.ToDoState;
import model.task.InProgressState;
import view.TaskManagerView;

import javax.swing.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskManagerViewTest {

    @Test
    void testSelectTaskStateInComboBox_selectFirstState() {
        // Arrange
        TaskState state1 = new ToDoState();
        TaskState state2 = new ToDoState().next();
        TaskState state3 = new ToDoState().next().next(); // Assuming you have this class

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