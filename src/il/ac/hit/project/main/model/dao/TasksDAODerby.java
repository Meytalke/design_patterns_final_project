package il.ac.hit.project.main.model.dao;

import il.ac.hit.project.main.model.task.*;

//Sql imports
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

//Util imports
import java.util.ArrayList;
import java.util.List;

import il.ac.hit.project.main.model.task.TaskState;
import il.ac.hit.project.main.model.task.ToDoState;

/**
 * A concrete implementation of the {@code ITasksDAO} interface that interacts with
 * a Derby database to manage tasks. This class uses a Singleton pattern to ensure
 * a single instance exists and maintains a persistent database connection throughout
 * the application's lifecycle.
 */
public class TasksDAODerby implements ITasksDAO {
    /*
     * TasksDAO Implementation to support DerbyDB.
     * Given support for the inherited functions for handling tasks data objects through
     * the DerbyDB jdbc driver, using the embedded mode.
     * [TasksDAODerby] instance - Singleton object
     * [Connection] connection - connection object to use to communicate with the DB
     * */
    // Singleton instance
    private static TasksDAODerby instance = null;
    private final Connection connection;

    /**
     * Private constructor to prevent direct instantiation
     * ensures proper connection to the DB.
     * @throws TasksDAOException If the driver or connection is missing
     */
    private TasksDAODerby() throws TasksDAOException {
        System.out.println("DEBUG: TasksDAODerby is connecting to the real DB.");
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            String DB_URL = "jdbc:derby:./taskDB;create=true";
            this.connection = DriverManager.getConnection(DB_URL);
            createTableIfNotExists(this.connection);
        } catch (ClassNotFoundException | SQLException e) {
            throw new TasksDAOException("Error initializing DB connection.", e);
        }
    }

    // A separate, public constructor for integration tests
    // This allows the test to manually provide a connection
    public TasksDAODerby(Connection connection) throws TasksDAOException {
        this.connection = connection;
        // The table creation logic can be handled here as well, if needed.
        createTableIfNotExists(this.connection);
    }

    /**
     * <p>
     * This method is synchronized to ensure that only one instance of this class is
     * created, even in a multithreaded environment. This is important when dealing with
     * a separate thread for the UI and a separate thread for the DB.
     * </p>
     *
     * @return the single instance of this class (thread-safe)
     * @throws TasksDAOException if there is an error initializing the DB connection
     */
    public static synchronized TasksDAODerby getInstance() throws TasksDAOException {
        if (instance == null) {
            instance = new TasksDAODerby();
        }
        return instance;
    }

    /**
     * Creates the task table in the database if it does not already exist. If the
     * table already exists, this method does nothing.
     *
     * @param connection The connection to the database to use when creating the table.
     * @throws TasksDAOException If there is an error creating the table (for example, if
     *         the table already exists with a different schema).
     */
    private void createTableIfNotExists(Connection connection) throws TasksDAOException {
        try (Statement derbyStatement = connection.createStatement()) {
            /*SQL in English: 
            Create a table called "tasks" with the following columns:
                - id: an auto-incrementing integer primary key
                - title: a string with a maximum length of 255 characters
                - description: a string with a maximum length of 1024 characters
                - state: a string with a maximum length of 50 characters
            */
            String sql = "CREATE TABLE tasks (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description VARCHAR(1024), " +
                    "state VARCHAR(50) NOT NULL)";
            derbyStatement.executeUpdate(sql);
        } catch (SQLException e) {
            // "X0Y32" indicates a table already exists
            if (e.getSQLState().equals("X0Y32")) {
                System.out.println("Table already exists. Skipping..");
            }
            else{
                throw new TasksDAOException("Error creating table", e);
            }
        }
    }

    /**
     * Converts a human-readable task state name into a corresponding TaskState instance.
     * Implementation note: starts from a ToDoState and advances using {@code next()} to minimize
     * additional allocations and keep state transitions consistent.
     *
     * @param stateStr the display name of the state to convert; must be one of the supported values
     * @return a TaskState instance corresponding to {@code stateStr}
     * @throws IllegalArgumentException if {@code stateStr} is not a recognized state name
     */
    private TaskState stateFromString(String stateStr) {
        //Starting with a ToDoState and advancing it using the next function to save up on new instances
        TaskState state = new ToDoState();
        return switch (stateStr) {
            case "To Do" -> state;
            case "In Progress" -> state.next();
            case "Completed" -> state.next().next();
            default -> throw new IllegalArgumentException("Unknown state: " + stateStr);
        };
    }

    /**
     * Retrieves all tasks from the database.
     *
     * @return {@code ITask[]} An array of all tasks in the database.
     * @throws TasksDAOException Occurs if there is a database access error when
     * retrieving tasks.
     */
    @Override
    public ITask[] getTasks() throws TasksDAOException {
        List<ITask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY id ASC";
        //tryWith block, automatically closes AutoCloseable classes
        try (ResultSet resultSet = runQuery(sql)) {
            while (resultSet.next()) {
                tasks.add(new Task(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        stateFromString(resultSet.getString("state"))
                ));
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Error retrieving tasks", e);
        }
        // Convert a list to array
        return tasks.toArray(ITask[]::new );
    }

    /**
     * Retrieves a task from the database with the given id.
     *
     * @param id The id of the task to retrieve.
     * @return The task with the given id, or null if the task does not exist.
     * @throws TasksDAOException If there is a database access error when retrieving a task.
     */
    @Override
    public ITask getTask(int id) throws TasksDAOException {
        //Select a specific task
        String sql = "SELECT * FROM tasks WHERE id = " + id;
        try(ResultSet resultSet = runQuery(sql))
        {
            //If we find any results... we return to a task.
            if (resultSet.next()) {
                return new Task(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        stateFromString(resultSet.getString("state"))
                );
            }
            else{
                //Explicit declaring that a task with that id does not exist.
                throw new TasksDAOException("No task with id " + id);
            }

        } catch (SQLException e) {
            throw new TasksDAOException("Error retrieving task", e);
        }

    }

    /**
     * Adds a task to the database.
     *
     * @param task The task to add.
     * @throws TasksDAOException If there is a database access error when adding a task.
     */
    @Override
    public void addTask(ITask task) throws TasksDAOException {
        //Clean data to avoid SQL injection
        String title = task.getTitle().replace("'", "''");
        String description = task.getDescription().replace("'", "''");
        String state = task.getState().getDisplayName();

        //Insert new task
        String sql = "INSERT INTO tasks (title, description, state) " +
                "VALUES ('" + title + "', '" + description + "', '" + state+ "')";
        try {
            int newId = runInsert(sql);
            ((Task) task).setId(newId);
        } catch (SQLException e) {
            throw new TasksDAOException("Error adding task", e);
        }
    }

    /**
     * Updates a task in the database.
     *
     * @param task The task to update.
     * @throws TasksDAOException If there is a database access error when updating a task.
     */
    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        //Update task
        String title = task.getTitle().replace("'", "''");
        String description = task.getDescription().replace("'", "''");
        String state = task.getState().getDisplayName();

        String sql = "UPDATE tasks SET " +
                "title = '" + title + "', " +
                "description = '" + description + "', " +
                "state = '" + state + "' " +
                "WHERE id = " + task.getId();

        try {
            runUpdate(sql);
        } catch (SQLException e) {
            throw new TasksDAOException("Error updating task", e);
        }
    }

    /**
     * Deletes all tasks from the database.
     *
     * @throws TasksDAOException If there is a database access error when deleting all tasks.
     */
    @Override
    public void deleteTasks() throws TasksDAOException {
        //Delete all tasks
        try {
            runUpdate( "DELETE FROM tasks");
        } catch (SQLException e) {
            throw new TasksDAOException("Error deleting all tasks", e);
        }
    }

    /**
     * Deletes a task from the database with the given id.
     *
     * @param id The id of the task to delete.
     * @throws TasksDAOException If there is a database access error when deleting a task.
     */
    @Override
    public void deleteTask(int id) throws TasksDAOException {
        String sql = "DELETE FROM tasks WHERE id = " + id;
        try {

            int affectedRows = runUpdate(sql);
            if (affectedRows == 0) {
                throw new SQLException("Deleting task failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Error deleting task", e);
        }
    }

    /**
     * Run a query (SELECT) and return its ResultSet.
     * The caller is responsible for processing the ResultSet.
     */
    private ResultSet runQuery(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql); // stmt will be closed when ResultSet is closed
    }

    /**
     * Run an update (INSERT, UPDATE, DELETE).
     */
    private int runUpdate(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
    }

    /**
     * Executes an INSERT SQL statement and returns the auto-generated primary key.
     * <p>
     * This method assumes that the target table has an auto-increment (identity) column.
     * It is mainly used when inserting new tasks, so the caller can update the in-memory
     * task object with the generated ID from the database.
     * </p>
     *
     * @param sql the SQL INSERT statement to execute
     * @return the auto-generated key (e.g., task ID)
     * @throws SQLException if the insert fails, no rows are affected, or no key is returned
     */
    private int runInsert(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            int affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            if (affectedRows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert failed, no ID obtained.");
            }
        }
    }
}