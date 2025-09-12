
package il.ac.hit.project.test.dao;

import il.ac.hit.project.main.model.dao.TasksDAODerby;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.Task;
import il.ac.hit.project.main.model.task.ToDoState;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@code TasksDAODerby} class using the Derby database.
 * These tests verify the behavior of database operations and exception handling
 * in various scenarios.
 */
class TasksDAODerbyUnitTest {

    /**
     * Tests that the getTasks method throws an exception when the database is invalid.
     * It mocks a database connection to simulate a query failure.
     *
     * @throws SQLException      if a SQL error occurs during mocking.
     * @throws TasksDAOException if the getTasks method throws the expected DAO exception.
     */
    @Test
    void testGetTasks_withInvalidDatabase_throwsException() throws SQLException, TasksDAOException {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement mockedStatement = mock(Statement.class);

        when(mockedConnection.createStatement()).thenReturn(mockedStatement);
        when(mockedStatement.execute(anyString())).thenReturn(true);

        when(mockedStatement.executeQuery(anyString()))
                .thenThrow(new SQLException("Simulated database query failure"));

        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Act & Assert
        assertThrows(TasksDAOException.class, tasksDAODerby::getTasks);
        verify(mockedStatement).executeQuery(anyString());
    }


    /**
     * Tests the addTask method for a successful operation by mocking database interactions.
     * It verifies that the method does not throw an exception.
     *
     * @throws Exception if an unexpected error occurs during the test.
     */

    @Test
    void testAddTask_successful() throws Exception {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement mockedStatement = mock(Statement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedConnection.createStatement()).thenReturn(mockedStatement);
        when(mockedStatement.execute(anyString())).thenReturn(true);

        when(mockedStatement.executeUpdate(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(1);
        when(mockedStatement.getGeneratedKeys()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, false);
        when(mockedResultSet.getInt(1)).thenReturn(100);

        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Act & Assert
        ITask task = new Task(0, "New Task", "Description", new ToDoState());
        assertDoesNotThrow(() -> tasksDAODerby.addTask(task));

        verify(mockedStatement).executeUpdate(anyString(), eq(Statement.RETURN_GENERATED_KEYS));
    }
}