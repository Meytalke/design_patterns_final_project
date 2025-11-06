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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@code TasksDAODerby} class using Mockito.
 * These tests verify the behavior of database operations and exception handling
 * in various scenarios by mocking the database connection.
 */
class TasksDAODerbyUnitTest {

    /**
     * Tests that the {@link TasksDAODerby#getTasks()} method throws a {@link TasksDAOException}
     * when an underlying {@link SQLException} occurs.
     */
    @Test
    void testGetTasks_withInvalidDatabase_throwsException() throws SQLException, TasksDAOException {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement mockedStatement = mock(Statement.class);

        // Mock the statement creation
        when(mockedConnection.createStatement()).thenReturn(mockedStatement);
        // Mock the query execution to throw an exception
        when(mockedStatement.executeQuery(anyString()))
                .thenThrow(new SQLException("Simulated database query failure"));

        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Act & Assert
        assertThrows(TasksDAOException.class, tasksDAODerby::getTasks);
        verify(mockedStatement).executeQuery(anyString());
    }

    /**
     * Tests that the {@link TasksDAODerby#addTask(ITask)} method successfully adds a task
     * and updates its ID by mocking the generated keys from the database.
     */
    @Test
    void testAddTask_successful() throws Exception {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement mockedStatement = mock(Statement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);
        ITask task = new Task(0, "New Task", "Description", new ToDoState());

        when(mockedConnection.createStatement()).thenReturn(mockedStatement);
        // Mock executeUpdate to return 1 (for 1 affected row)
        when(mockedStatement.executeUpdate(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(1);
        // Mock the ResultSet for generated keys
        when(mockedStatement.getGeneratedKeys()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, false); // Return true once, then false
        when(mockedResultSet.getInt(1)).thenReturn(100); // The new ID

        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Act & Assert
        assertDoesNotThrow(() -> tasksDAODerby.addTask(task));
        assertEquals(100, task.getId()); // Verify the ID was updated

        verify(mockedStatement).executeUpdate(anyString(), eq(Statement.RETURN_GENERATED_KEYS));
        verify(mockedResultSet).next();
        verify(mockedResultSet).getInt(1);
    }

    /**
     * Tests that the {@link TasksDAODerby#getTask(int)} method returns a task when
     * a valid ID is provided. It mocks the database to return a single result.
     */
    @Test
    void testGetTask_validId_returnsTask() throws SQLException, TasksDAOException {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement mockedStatement = mock(Statement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);
        int taskId = 1;

        when(mockedConnection.createStatement()).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery(anyString())).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, false); // Return one result
        when(mockedResultSet.getInt("id")).thenReturn(taskId);
        when(mockedResultSet.getString("title")).thenReturn("Test Task");
        when(mockedResultSet.getString("description")).thenReturn("Test Description");
        when(mockedResultSet.getString("state")).thenReturn("To Do");

        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Act
        ITask result = tasksDAODerby.getTask(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Test Task", result.getTitle());
    }

    /**
     * Tests that the {@link TasksDAODerby#updateTask(ITask)} method does not throw an
     * exception on a successful update. It verifies that the `executeUpdate` method
     * is called correctly.
     */
    @Test
    void testUpdateTask_successful() throws Exception {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement mockedStatement = mock(Statement.class);
        ITask taskToUpdate = new Task(1, "Updated Title", "Updated Description", new ToDoState());

        when(mockedConnection.createStatement()).thenReturn(mockedStatement);
        when(mockedStatement.executeUpdate(anyString())).thenReturn(1);

        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Act & Assert
        assertDoesNotThrow(() -> tasksDAODerby.updateTask(taskToUpdate));
        verify(mockedStatement).executeUpdate(contains("UPDATE tasks SET"));
    }

    /**
     * Tests that the {@link TasksDAODerby#updateTask(ITask)} method handles a database
     * exception correctly by throwing a {@link TasksDAOException}.
     */
    @Test
    void testUpdateTask_withInvalidDatabase_throwsException() throws Exception {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement mockedStatementForSetup = mock(Statement.class); // For constructor
        Statement mockedStatementForTest = mock(Statement.class);  // For test

        // Mock the behavior of createStatement()
        when(mockedConnection.createStatement())
                .thenReturn(mockedStatementForSetup) // The first call returns a statement that doesn't throw
                .thenReturn(mockedStatementForTest); // Later calls return a statement that throws

        // Mock the setup statement to succeed
        when(mockedStatementForSetup.executeUpdate(anyString())).thenReturn(1);

        // Instantiate the DAO, the constructor will use mockedStatementForSetup
        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Now, mock the statement for the actual test method to throw an exception
        when(mockedStatementForTest.executeUpdate(anyString()))
                .thenThrow(new SQLException("Simulated update failure", "42X01"));

        ITask taskToUpdate = new Task(1, "Title", "Description", new ToDoState());

        // Act & Assert
        assertThrows(TasksDAOException.class, () -> tasksDAODerby.updateTask(taskToUpdate));
        verify(mockedStatementForTest).executeUpdate(anyString());
    }

    /**
     * Tests that the {@link TasksDAODerby#deleteTask(int)} method throws a {@link TasksDAOException}
     * when no rows are affected by the delete operation. This simulates deleting a task
     * that does not exist in the database.
     */
    @Test
    void testDeleteTask_nonExistentId_throwsException() throws SQLException, TasksDAOException {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement setupStatement = mock(Statement.class);
        Statement testStatement = mock(Statement.class);

        // Declare and initialize the missing variable
        int nonExistentId = 999;

        // Mock the behavior of createStatement() for both setup and test
        when(mockedConnection.createStatement())
                .thenReturn(setupStatement)
                .thenReturn(testStatement);

        // Mock the setup statement to succeed
        when(setupStatement.executeUpdate(anyString())).thenReturn(0);

        // Instantiate the DAO, the constructor will use 'setupStatement'
        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Now, set up the behavior for the 'testStatement' specifically for the deleteTask method
        when(testStatement.executeUpdate(anyString())).thenReturn(0);

        // Act & Assert
        TasksDAOException exception = assertThrows(TasksDAOException.class, () -> tasksDAODerby.deleteTask(nonExistentId));

        // Verify the exception's message is correct.
        assertTrue(exception.getCause().getMessage().contains("Deleting task failed, no rows affected."));
        // Verify that the executeUpdate was called on the correct statement.
        verify(testStatement).executeUpdate(anyString());
    }
}