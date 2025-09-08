package test.model.dao;

import model.dao.TasksDAODerby;
import model.dao.TasksDAOException;
import model.task.ITask;
import model.task.Task;
import model.task.ToDoState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TasksDAODerbyIntegrationTest {

    private TasksDAODerby tasksDAODerby;
    private final String TEST_DB_URL = "jdbc:derby:./testTaskDB;create=true";
    private final String SHUTDOWN_DB_URL = "jdbc:derby:./testTaskDB;shutdown=true";
    private final File dbDirectory = new File("./testTaskDB");

    @BeforeEach
    void setUp() throws TasksDAOException, SQLException {
        // 1. Ensure a clean slate by deleting any existing test database.
        deleteTestDbDirectory();

        // 2. Create a new test database connection and initialize the DAO.
        try {
            this.tasksDAODerby = new TasksDAODerby(DriverManager.getConnection(TEST_DB_URL));
        } catch (SQLException e) {
            throw new TasksDAOException("Could not connect to test database.", e);
        }
    }

    @AfterEach
    void tearDown() throws TasksDAOException {
        // 1. Shut down the test database.
        try {
            DriverManager.getConnection(SHUTDOWN_DB_URL);
        } catch (SQLException e) {
            // Derby throws an "08006" state code on successful shutdown.
            if (!e.getSQLState().equals("08006")) {
                throw new TasksDAOException("Error shutting down test database.", e);
            }
        } finally {
            // 2. Always attempt to delete the test database directory.
            deleteTestDbDirectory();
        }
    }

    // Helper method to safely delete the test database directory
    private void deleteTestDbDirectory() {
        if (dbDirectory.exists()) {
            try {
                // Use Files.walk to delete files and directories more robustly.
                // It walks the directory tree and deletes items in reverse order.
                Files.walk(dbDirectory.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (Exception e) {
                System.err.println("Failed to delete test database directory: " + e.getMessage());
            }
        }
    }

    @Test
    void testGetTasks_emptyDatabase_returnsEmptyArray() throws TasksDAOException {
        ITask[] tasks = tasksDAODerby.getTasks();
        assertNotNull(tasks);
        assertEquals(0, tasks.length);
    }

    @Test
    void testAddAndGetTasks() throws TasksDAOException {
        ITask task1 = new Task(0, "Task 1", "Description 1", new ToDoState());
        ITask task2 = new Task(0, "Task 2", "Description 2", new ToDoState());
        tasksDAODerby.addTask(task1);
        tasksDAODerby.addTask(task2);

        ITask[] tasks = tasksDAODerby.getTasks();

        assertNotNull(tasks);
        assertEquals(2, tasks.length);
        assertEquals("Task 1", tasks[0].getTitle());
        assertEquals("Task 2", tasks[1].getTitle());
    }

    @Test
    void testDeleteAllTasks() throws TasksDAOException {
        ITask task = new Task(0, "Temp Task", "Temp Description", new ToDoState());
        tasksDAODerby.addTask(task);
        tasksDAODerby.deleteTasks();
        ITask[] tasks = tasksDAODerby.getTasks();
        assertNotNull(tasks);
        assertEquals(0, tasks.length);
    }
}