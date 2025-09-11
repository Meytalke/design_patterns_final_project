<<<<<<<< HEAD:src/main/java/Main.java
package il.ac.hit.project.main.java;
========
package il.ac.hit.project.main;
>>>>>>>> master:src/il/ac/hit/project/main/Main.java

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.dao.TasksDAODerby;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.dao.TasksDAOProxy;
import il.ac.hit.project.main.view.IView;
import il.ac.hit.project.main.view.TaskManagerView;
import il.ac.hit.project.main.viewmodel.IViewModel;
import il.ac.hit.project.main.viewmodel.TasksViewModel;

import javax.swing.*;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Application entry point.
 * <p>
 * Boots the UI on the Swing Event Dispatch Thread (EDT), wires the data layer (DAO),
 * wraps it with a caching proxy, constructs the ViewModel and the View, and starts the UI.
 * Also registers a JVM shutdown hook to gracefully stop the ViewModel and shut down the
 * embedded Derby database.
 *
 * <h3>Startup flow</h3>
 * <ol>
 *   <li>Obtain a singleton {@link ITasksDAO} implementation (Derby-backed).</li>
 *   <li>Wrap it with {@link TasksDAOProxy} for caching.</li>
 *   <li>Create the {@link il.ac.hit.project.main.view.TaskManagerView} and {@link il.ac.hit.project.main.viewmodel.TasksViewModel}.</li>
 *   <li>Wire ViewModel ↔ View and start the UI on the EDT.</li>
 * </ol>
 *
 * <h3>Shutdown</h3>
 * A JVM shutdown hook attempts to:
 * <ul>
 *   <li>Invoke {@link TasksViewModel#shutdown()} if the ViewModel is present.</li>
 *   <li>Shut down the Derby database (a successful shutdown raises an SQLException with SQLState 08006).</li>
 * </ul>
 *
 * <h3>Threading</h3>
 * UI-related operations are scheduled via {@link SwingUtilities#invokeLater(Runnable)} to ensure they run on the EDT.
 */
public class Main {

    /**
     * Program entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        // Container to make the ViewModel visible to the shutdown hook (captured by reference).
        final IViewModel[] viewModelContainer = new IViewModel[1];

        // Initialize and start the UI on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a single instance of the real DAO (Singleton)
                ITasksDAO tasksDAO = TasksDAODerby.getInstance();

                // Wrap the real DAO with a Proxy for caching
                ITasksDAO proxyDAO = new TasksDAOProxy(tasksDAO);

                // Construct the View and ViewModel and wire them together.
                IView taskManagerView = new TaskManagerView();
                IViewModel viewModel = new TasksViewModel(proxyDAO,  taskManagerView);
                viewModelContainer[0] = viewModel;

                taskManagerView.setViewModel(viewModel);
                System.out.println("System starting");
                taskManagerView.start();

            } catch (TasksDAOException e) {
                // Surface the error to the user via a dialog and log the stack trace.
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });

        //Attempt to ensure the database is shutdown upon shutting down the program.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Give the ViewModel a chance to release resources.
                if (viewModelContainer[0] instanceof TasksViewModel) {
                    ((TasksViewModel) viewModelContainer[0]).shutdown();
                }

                // Derby's proper shutdown throws an SQLException with SQLState "08006".
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
                System.out.println("Derby database shut down successfully.");
            } catch (SQLException e) {
                if (e.getSQLState().equals("08006")) {
                    System.out.println("Derby database shut down successfully.");
                } else {
                    System.err.println("Error shutting down Derby: " + e.getMessage());
                }
            }
        }));
    }
}