package model.dao;

public class TasksDAOException extends Exception {
    public TasksDAOException(String message, Throwable cause) {
        super(message, cause);
    }
    public TasksDAOException(String message) {super( message );}
}
