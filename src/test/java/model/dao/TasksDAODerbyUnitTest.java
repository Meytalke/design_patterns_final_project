package il.ac.hit.project.test.model.dao;

import il.ac.hit.project.main.model.dao.TasksDAODerby;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.Task;
import il.ac.hit.project.main.model.task.ToDoState;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TasksDAODerbyUnitTest {

    @Test
    void testGetTasks_withInvalidDatabase_throwsException() throws SQLException, TasksDAOException {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement mockedStatement = mock(Statement.class);

        /*
        FIXME:
            You need to mock the behavior of `createTableIfNotExists` within the constructor.
            The constructor's call to createStatement() and then execute() must be mocked to
            succeed, otherwise the test fails on initialization.
        */
        when(mockedConnection.createStatement()).thenReturn(mockedStatement);
        // We're mocking the behavior of `Statement.execute()` which is a boolean method, not void.
        // So we use when(...).thenReturn(...)
        when(mockedStatement.execute(anyString())).thenReturn(true);

        // Mock the behavior of `executeQuery` to throw an exception
        when(mockedStatement.executeQuery(anyString()))
                .thenThrow(new SQLException("Simulated database query failure"));

        // Inject the mocked connection into the TasksDAODerby class instance.
        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Act & Assert
        // Assert that calling getTasks() throws the expected exception.
        assertThrows(TasksDAOException.class, tasksDAODerby::getTasks);

        // Verify that the executeQuery method was called
        verify(mockedStatement).executeQuery(anyString());
    }
    @Test
    void testAddTask_successful() throws Exception {
        // Arrange
        Connection mockedConnection = mock(Connection.class);
        Statement mockedStatement = mock(Statement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        // מדמה את יצירת הטבלה על מנת שהבנאי לא יזרוק חריגה
        when(mockedConnection.createStatement()).thenReturn(mockedStatement);
        when(mockedStatement.execute(anyString())).thenReturn(true);

        // מדמה את הוספת המשימה בהצלחה
        when(mockedStatement.executeUpdate(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(1);

        // מדמה את ה-ResultSet שמוחזר מ-getGeneratedKeys
        when(mockedStatement.getGeneratedKeys()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, false); // מדמה שיש שורה אחת של מפתח שנוצר
        when(mockedResultSet.getInt(1)).thenReturn(100); // מדמה את מזהה המשימה החדש

        TasksDAODerby tasksDAODerby = new TasksDAODerby(mockedConnection);

        // Act & Assert
        ITask task = new Task(0, "New Task", "Description", new ToDoState());
        assertDoesNotThrow(() -> tasksDAODerby.addTask(task));

        // ודא שהמתודה updateTask נקראה עם הפרמטרים הנכונים
        verify(mockedStatement).executeUpdate(anyString(), eq(Statement.RETURN_GENERATED_KEYS));
    }
}