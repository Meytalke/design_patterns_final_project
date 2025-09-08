package model.dao;

import model.task.*;

//Sql imports
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;

//Util imports
import java.util.ArrayList;
import java.util.List;

import model.task.ITaskState;
import model.task.ToDoState;

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
    private final String DB_URL = "jdbc:derby:./taskDB;";

    /**
     * Private constructor to prevent direct instantiation
     * ensures proper connection to the DB.
     * @throws TasksDAOException If the driver or connection is missing
     */
    private TasksDAODerby() throws TasksDAOException {
        try {
            // Load the driver (not strictly necessary for Java 7+ but good practice)
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            connection = DriverManager.getConnection(DB_URL);
            createTableIfNotExists(connection);
        } catch (ClassNotFoundException | SQLException e) {
            throw new TasksDAOException("Error initializing DB connection.", e);
        }
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
                - creation_date: the date and time the task was created
                - priority: a string to represent the task's priority level
            */
            String sql = "CREATE TABLE tasks (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description VARCHAR(1024), " +
                    "state VARCHAR(50) NOT NULL, " +
                    "creation_date TIMESTAMP NOT NULL, " +
                    "priority VARCHAR(20) NOT NULL)";
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
     * @return a ITaskState instance corresponding to {@code stateStr}
     * @throws IllegalArgumentException if {@code stateStr} is not a recognized state name
     */
    private ITaskState stateFromString(String stateStr) {
        //Starting with a ToDoState and advancing it using the next function to save up on new instances
        ITaskState state = new ToDoState();
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
        String sql = "SELECT * FROM tasks";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            // While we have any results (if at all), extract data into an object and add to the list.
            while (resultSet.next()) {
                tasks.add(new Task(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        stateFromString(resultSet.getString("state")),
                        resultSet.getTimestamp("creation_date"),
                        TaskPriority.valueOf(resultSet.getString("priority"))
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
        try{
            //Select a specific task
            String sql = "SELECT * FROM tasks WHERE id = " + id;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            //If we find any results... we return a task.
            if (resultSet.next()) {
                return new Task(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        stateFromString(resultSet.getString("state")),
                        resultSet.getTimestamp("creation_date"),
                        TaskPriority.valueOf(resultSet.getString("priority"))
                );
            }
            
        } catch (SQLException e) {
            throw new TasksDAOException("Error retrieving task", e);
        }
        /*If we haven't found any results, we throw an exception instead of 
        returning null to explicitly indicate that the task doesn't exist.*/
        throw new TasksDAOException("Task not found");
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
        Timestamp creationTimestamp = new Timestamp(task.getCreationDate().getTime());
        System.out.println("Timestamp creationTimestamp:");
        System.out.println(creationTimestamp.toString());
        String priority = task.getPriority().toString();

        //Insert new task
        String sql = "INSERT INTO tasks (title, description, state, creation_date, priority) " +
                "VALUES ('" + title + "', '" + description + "', '" + state + "', '" + creationTimestamp + "', '" + priority + "')";

        try {
            Statement statement = connection.createStatement();
            // Get affected rows value to validate insert
            int affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            if (affectedRows == 0)
                throw new SQLException("Creating task failed, no rows affected.");

            ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    // Get the auto-generated ID
                    int newId = generatedKeys.getInt(1);
                    // Update the task object with the new ID
                    ((Task) task).setId(newId);
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }

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
        String priority = task.getPriority().toString();

        String sql = "UPDATE tasks SET " +
                "title = '" + title + "', " +
                "description = '" + description + "', " +
                "state = '" + state + "', " +
                "priority = '" + priority + "' " +
                "WHERE id = " + task.getId();

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
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
        String sql = "DELETE FROM tasks";
        try {
            Statement statement = connection.createStatement(); 
            statement.executeUpdate(sql);
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
            Statement statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows == 0) {
                throw new SQLException("Deleting task failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Error deleting task", e);
        }
    }
}