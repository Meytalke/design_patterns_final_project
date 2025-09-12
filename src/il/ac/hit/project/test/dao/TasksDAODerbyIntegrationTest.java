package il.ac.hit.project.test.dao;

import il.ac.hit.project.main.model.dao.TasksDAODerby;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.Task;
import il.ac.hit.project.main.model.task.ToDoState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Integration tests for the {@link TasksDAODerby} class.
 * <p>
 * These tests use an embedded Derby database to validate that the DAO
 * correctly performs CRUD operations (create, read, delete).
 * A fresh database is created before each test and removed afterwards
 * to ensure isolation between test cases.
 * </p>
 */
class TasksDAODerbyIntegrationTest {

    /** The DAO under test. */
    private TasksDAODerby tasksDAODerby;
    /** JDBC connection string for the test Derby database. */
    private final String TEST_DB_URL = "jdbc:derby:./testTaskDB;create=true";
    /** JDBC connection string to shut down the test Derby database. */
    private final String SHUTDOWN_DB_URL = "jdbc:derby:./testTaskDB;shutdown=true";
    /** File reference to the database directory on disk. */
    private final File dbDirectory = new File("./testTaskDB");

    /**
     * Prepares a clean database and initializes the DAO before each test.
     *
     * @throws TasksDAOException if the DAO cannot be created
     * @throws SQLException      if there is a problem creating the test DB connection
     */
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

    /**
     * Cleans up after each test by shutting down the database and deleting files.
     *
     * @throws TasksDAOException if shutdown fails for reasons other than Derby's expected shutdown code
     */
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

    /**
     * Helper method to delete the Derby database directory if it exists.
     * <p>
     * Uses {@link Files#walk(Path, FileVisitOption...)} to recursively remove all files and subdirectories.
     * </p>
     */
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

    /**
     * Verifies that an empty database returns an empty array of tasks.
     *
     * @throws TasksDAOException if an error occurs while fetching tasks
     */
    @Test
    void testGetTasks_emptyDatabase_returnsEmptyArray() throws TasksDAOException {
        ITask[] tasks = tasksDAODerby.getTasks();
        assertNotNull(tasks);
        assertEquals(0, tasks.length);
    }

    /**
     * Tests adding tasks and verifying they are correctly retrieved from the database.
     *
     * @throws TasksDAOException if an error occurs while adding or fetching tasks
     */
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

    /**
     * Tests that deleting all tasks clears the database.
     *
     * @throws TasksDAOException if an error occurs while adding or deleting tasks
     */
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