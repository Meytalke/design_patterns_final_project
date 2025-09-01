package model.dao;

import model.task.ITask;
import model.task.Task;
import model.task.TaskState;

//Sql imports
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

//Util imports
import java.util.ArrayList;
import java.util.List;

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
    private Connection connection = null;
    private final String DB_URL = "jdbc:derby:./taskDB;create=true";

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
     * created, even in a multi-threaded environment. This is important when dealing with
     * a seperate thread for the UI and a seperate thread for the DB.
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
     * Creates the tasks table in the database if it does not already exist. If the
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
            // "X0Y32" indicates table already exists
            if (!e.getSQLState().equals("X0Y32")) {
                throw new TasksDAOException("Error creating table", e);
            }
        }
    }

    /** 
     * Retrieves all tasks from the database.
     *
     * @return {@code ITask[]} An array of all tasks in the database.
     * @throws TasksDAOException Occurs if there's is database access error when 
     * retrieving tasks.
     */
    @Override
    public ITask[] getTasks() throws TasksDAOException {
        List<ITask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            // While we have any results (if at all), extract data into object and add to list.
            while (resultSet.next()) {
                tasks.add(new Task(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        TaskState.valueOf(resultSet.getString("state"))
                ));
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Error retrieving tasks", e);
        }
        // Convert list to array
        return tasks.toArray(ITask[]::new );
    }

    /**
     * Retrieves a task from the database with the given id.
     *
     * @param id The id of the task to retrieve.
     * @return The task with the given id, or null if the task does not exist.
     * @throws TasksDAOException If there's is database access error when retrieving task.
     */
    @Override
    public ITask getTask(int id) throws TasksDAOException {
        try{
            //Select specific task
            String sql = "SELECT * FROM tasks WHERE id = " + id;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            //If we find any results... we return a task.
            if (resultSet.next()) {
                return new Task(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        TaskState.valueOf(resultSet.getString("state"))
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
     * @throws TasksDAOException If there's is database access error when adding task.
     */
    @Override
    public void addTask(ITask task) throws TasksDAOException {
        //Insert new task
        String sql = 
        "INSERT INTO tasks (title, description, state) "+
        "VALUES (" +task.getTitle()+", "+task.getDescription()+", "+task.getState().name()+")";

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
                    // Why?
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
     * @throws TasksDAOException If there's is database access error when updating task.
     */
    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        //Update task
        String sql = "UPDATE tasks SET "+
                "title = "+ task.getTitle()+
                ", description = "+task.getDescription()+
                ", state = "+task.getState().name()+
                " WHERE id = "+task.getId();
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
     * @throws TasksDAOException If there's is database access error when deleting all tasks.
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
     * @throws TasksDAOException If there's is database access error when deleting task.
     */
    @Override
    public void deleteTask(int id) throws TasksDAOException {
        //Delete task
        String sql = "DELETE FROM tasks WHERE id = "+id;
        try {
            Statement statement = connection.prepareStatement(sql);
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new TasksDAOException("Error deleting task", e);
        }
    }
}
