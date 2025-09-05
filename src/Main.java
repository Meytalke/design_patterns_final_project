
import model.dao.ITasksDAO;
import model.dao.TasksDAODerby;
import model.dao.TasksDAOException;
import model.dao.TasksDAOProxy;
import view.IView;
import view.TaskManagerView;
import viewmodel.IViewModel;
import viewmodel.TasksViewModel;

import javax.swing.*;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        final IViewModel[] viewModelContainer = new IViewModel[1];
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a single instance of the real DAO (Singleton)
                ITasksDAO tasksDAO = TasksDAODerby.getInstance();

                // Wrap the real DAO with a Proxy for caching
                ITasksDAO proxyDAO = new TasksDAOProxy(tasksDAO);

                IView taskManagerView = new TaskManagerView();

                // The ViewModel receives the Proxy and the View
                IViewModel viewModel = new TasksViewModel(proxyDAO,  taskManagerView);
                viewModelContainer[0] = viewModel;
                // The View receives the ViewModel
                taskManagerView.setViewModel(viewModel);
                System.out.println("System starting");
                taskManagerView.start();

            } catch (TasksDAOException e) {
                //Print the error to a popup dialog
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });

        //Attempt to ensure the database is shutdown upon shutting down the program.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (viewModelContainer[0] instanceof TasksViewModel) {
                    ((TasksViewModel) viewModelContainer[0]).shutdown();
                }

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