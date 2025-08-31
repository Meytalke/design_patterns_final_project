package model.dao;

import model.task.ITask;
import model.task.Task;
import model.task.TaskState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TasksDAOImpl implements ITasksDAO {

    // Singleton instance
    private static TasksDAOImpl instance;
    private static Connection connection = null;
    private static final String DB_URL = "jdbc:derby:./taskDB;create=true";

    // Private constructor to prevent direct instantiation
    public TasksDAOImpl() throws TasksDAOException {
        try {
            // Load the driver (not strictly necessary for Java 7+ but good practice)
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            connection = DriverManager.getConnection(DB_URL);
            createTableIfNotExists(connection);
        } catch (ClassNotFoundException | SQLException e) {
            throw new TasksDAOException("Error initializing DB connection.", e);
        }
    }

    // Public static method to get the single instance (thread-safe)
    public static synchronized TasksDAOImpl getInstance() throws TasksDAOException {
        if (instance == null) {
            instance = new TasksDAOImpl();
        }
        return instance;
    }

    private void createTableIfNotExists(Connection conn) throws TasksDAOException {
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE tasks (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description VARCHAR(1024), " +
                    "state VARCHAR(50) NOT NULL)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            if (!e.getSQLState().equals("X0Y32")) { // "X0Y32" indicates table already exists
                throw new TasksDAOException("Error creating table", e);
            }
        }
    }

    @Override
    public ITask[] getTasks() throws TasksDAOException {
        List<ITask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        TaskState.valueOf(rs.getString("state"))
                ));
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Error retrieving tasks", e);
        }
        return tasks.toArray(new ITask[0]);
    }

    @Override
    public ITask getTask(int id) throws TasksDAOException {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            TaskState.valueOf(rs.getString("state"))
                    );
                }
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Error retrieving task", e);
        }
        return null;
    }

    @Override
    public void addTask(ITask task) throws TasksDAOException {
        String sql = "INSERT INTO tasks (title, description, state) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getState().name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Get the auto-generated ID
                    int newId = generatedKeys.getInt(1);
                    // Update the task object with the new ID
                    ((Task) task).setId(newId);
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new TasksDAOException("Error adding task", e);
        }
    }

    @Override
    public void updateTask(ITask task) throws TasksDAOException {
        String sql = "UPDATE tasks SET title = ?, description = ?, state = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getState().name());
            pstmt.setInt(4, task.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("Error updating task", e);
        }
    }

    @Override
    public void deleteTasks() throws TasksDAOException {
        String sql = "DELETE FROM tasks";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new TasksDAOException("Error deleting all tasks", e);
        }
    }

    @Override
    public void deleteTask(int id) throws TasksDAOException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new TasksDAOException("Error deleting task", e);
        }
    }
}
