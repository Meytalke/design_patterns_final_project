import model.dao.*;
import model.dao.TasksDAODerby;
import view.TaskManagerUI;
import viewmodel.TasksViewModel;

import javax.swing.*;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a single instance of the real DAO (Singleton)
                ITasksDAO tasksDAO = TasksDAODerby.getInstance();

                // Wrap the real DAO with a Proxy for caching
                ITasksDAO proxyDAO = new TasksDAOProxy(tasksDAO);

                // The ViewModel receives the Proxy
                TasksViewModel viewModel = new TasksViewModel(proxyDAO);

                // he View receives the ViewModel
                TaskManagerUI ui = new TaskManagerUI(viewModel);
                ui.start();

            } catch (TasksDAOException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
                System.out.println("Derby database shut down successfully.");
            } catch (SQLException e) {
                // A successful shutdown in Derby throws an SQLException with a specific state "08006"
                if (e.getSQLState().equals("08006")) {
                    System.out.println("Derby database shut down successfully.");
                } else {
                    System.err.println("Error shutting down Derby: " + e.getMessage());
                }
            }
        }));
    }
}