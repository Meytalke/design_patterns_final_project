// TasksViewModelTest.java
package il.ac.hit.project.test.viewmodel;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.dao.TasksDAODerby;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.report.IReportExporter;
import il.ac.hit.project.main.model.report.ReportData;
import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.view.TaskManagerView;
import org.junit.jupiter.api.*;

import il.ac.hit.project.main.view.IView;
import il.ac.hit.project.main.view.MessageType;
import il.ac.hit.project.main.viewmodel.TasksViewModel;
import org.mockito.ArgumentMatchers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TasksViewModelTest {

    /**
     * Test class for the TasksViewModel.
     * It verifies the behavior of the loadTasks method under different scenarios.
     */

    @Test
    public void testLoadTasksSuccess() throws Exception {
        // Arrange
        ITasksDAO mockTasksDAO = mock(ITasksDAO.class);
        IView mockView = mock(IView.class);
        ExecutorService mockExecutorService = mock(ExecutorService.class);
        List<ITask> mockTasks = List.of(new Task(1, "Task 1", "Description 1", new ToDoState()),
                new Task(2, "Task 2", "Description 2", new ToDoState()));
        when(mockTasksDAO.getTasks()).thenReturn(mockTasks.toArray(new ITask[0]));

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            latch.countDown();
            return mock(Future.class);
        }).when(mockExecutorService).submit(any(Runnable.class));

        TasksViewModel viewModel = new TasksViewModel(mockTasksDAO, mockView, mockExecutorService);

        // Act
        viewModel.loadTasks();
        latch.await(2, TimeUnit.SECONDS);

        // Assert
        assertEquals(mockTasks.size(), viewModel.getAllTasks().size());
        assertEquals(mockTasks, viewModel.getAllTasks());
        verify(mockTasksDAO, times(1)).getTasks();
    }

    /**
     * Tests that the {@link TasksViewModel#loadTasks()} method handles exceptions gracefully.
     * It ensures an error message is shown to the user and the task list remains empty.
     */
    @Test
    public void testLoadTasksHandlesException() throws Exception {
        // Arrange
        ITasksDAO mockTasksDAO = mock(ITasksDAO.class);
        IView mockView = mock(IView.class);
        ExecutorService mockExecutorService = mock(ExecutorService.class);

        // Mock the DAO to throw an exception
        when(mockTasksDAO.getTasks()).thenThrow(new TasksDAOException("Test Exception"));

        // Use a CountDownLatch to control async execution
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            latch.countDown();
            return null;
        }).when(mockExecutorService).submit(any(Runnable.class));

        TasksViewModel viewModel = new TasksViewModel(mockTasksDAO, mockView, mockExecutorService);

        // Act
        viewModel.loadTasks();
        latch.await(2, TimeUnit.SECONDS);

        // Assert
        // Verify that the showMessage() method was called with an error type
        verify(mockView, times(1)).showMessage(anyString(), eq(MessageType.ERROR));
        assertTrue(viewModel.getAllTasks().isEmpty());
        verify(mockTasksDAO, times(1)).getTasks();
    }

    /**
     * Tests that the {@link TasksViewModel#loadTasks()} method correctly updates the task list.
     * It verifies that the ViewModel's internal list matches the tasks returned by the DAO
     * and that the view is notified of the change.
     */
    @Test
    public void testLoadTasksUpdatesTaskList() throws Exception {
        // Arrange
        ITasksDAO mockTasksDAO = mock(ITasksDAO.class);
        IView mockView = mock(IView.class);
        ExecutorService mockExecutorService = mock(ExecutorService.class);
        List<ITask> mockTasks = List.of(new Task(1, "Sample Task", "Sample Description", new ToDoState()));
        when(mockTasksDAO.getTasks()).thenReturn(mockTasks.toArray(new ITask[0]));

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            latch.countDown();
            return mock(Future.class);
        }).when(mockExecutorService).submit(any(Runnable.class));

        TasksViewModel viewModel = new TasksViewModel(mockTasksDAO, mockView, mockExecutorService);

        // Act
        viewModel.loadTasks();
        latch.await(2, TimeUnit.SECONDS);

        // Assert
        assertEquals(new ArrayList<>(mockTasks), viewModel.getTasksList().get());
        verify(mockTasksDAO, times(1)).getTasks();
        verify(mockView, times(1)).setTasks(anyList()); // Use anyList() for flexibility
    }


    @Nested
    @DisplayName("Add Task tests")
    class AddTaskTests {
        /*
         * Test scenarios:
         * 1. Task added on DB correctly
         * 2. DB throws an error
         *
         * We don't cover the option that the string is empty because
         * we assume if the title is empty that this function will not run,
         * it being conditioned in addButtonPressed().
         * */

        TasksViewModel viewModel;
        //Define behavior on these variables
        TasksDAODerby tasksDAO;
        TaskManagerView view;
        //Add latch to notify thread when addTask is finished
        CountDownLatch latch =  new CountDownLatch(1);
        //Console logger control (two variables to control the stream to the terminal)
        ByteArrayOutputStream outContent;
        PrintStream originalOut;
        ByteArrayOutputStream errContent;
        PrintStream originalErr;
        @BeforeEach
        void beforeEach() {
            view = mock(TaskManagerView.class);
            tasksDAO = mock(TasksDAODerby.class);

            viewModel = new TasksViewModel(tasksDAO, view);

            /*
            Since the addTask function runs on a new service, we must await its products
            Create a latch to invoke a notification when the thread finished preforming the function
            */
            doAnswer(invocation -> {
                latch.countDown(); // signal that showMessage was called
                return null;
            }).when(view).showMessage(anyString(), any());

            // Arrange: redirect System.out
            outContent = new ByteArrayOutputStream();
            originalOut = System.out;
            errContent = new ByteArrayOutputStream();
            originalErr = System.err;

            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }
        @AfterEach
        void afterEach(){
            // Reset System.out and System.err
            System.setOut(originalOut);
            System.setErr(originalErr);
            // Reset taskList
            viewModel.getTasksList().get().clear();
        }

        /**
         * Tests the successful addition of a task.
         * Verifies that the task is added to the DAO, the ViewModel's list is updated,
         * and a success message is displayed to the user.
         */
        @Test
        void testAddTask_successful() throws Exception {

            String title = "Drink food";
            String description = "";

            //Invoke test action
            viewModel.addTask(title,description);
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            //Ensure print to console correctly
            assertEquals("Attempting to add task: " + title + "\nDesc: " + description + System.lineSeparator() + "Updating task list" + System.lineSeparator(), outContent.toString());
            //Ensure model was called to addTask
            verify(tasksDAO, times(1)).addTask(any(Task.class));
            //Ensure the tasksList was updated and listeners were notified
            //Verify UI update
            verify(view, times(1)).setTasks(ArgumentMatchers.<List<ITask>>any());
            ITask checkTask = null;
            //Search for a task in the tasksList
            checkTask = viewModel.getTasksList().get().stream().filter((t)->t.getTitle().equals(title)).toList().getFirst();
            assertNotNull(checkTask);
            //Ensure view was told to show a success message
            verify(view).showMessage("Task \"" + title + "\" added successfully!", MessageType.SUCCESS);
        }

        /**
         * Tests that the {@link TasksViewModel#addTask(String, String)} method handles a database exception gracefully.
         * It verifies that the DAO is called, an error message is shown, and the in-memory lists are not affected.
         */
        @Test
        void testAddTask_failed() throws Exception {
            String title = "";
            String description = "";

            // Mock DAO to throw an exception on addTask
            doThrow(new TasksDAOException(
                    "Error adding task",
                    new SQLException("some sql error")))
                    .when(tasksDAO).addTask(any(Task.class));

            // Invoke method under test
            viewModel.addTask(title, description);

            // Wait for the async update to finish
            boolean finished = latch.await(3, TimeUnit.SECONDS);
            assertTrue(finished, "Timeout waiting for async update");

            // Change System.lineSeperator() to \n
            String actualOut = outContent.toString().replaceAll("\\r?\\n", "\n").trim();
            String actualErr = errContent.toString().replaceAll("\\r?\\n", "\n").trim();

            // Print actual output for debugging (optional)
            System.out.println("ACTUAL_OUT: [" + actualOut + "]");
            System.out.println("ACTUAL_ERR: [" + actualErr + "]");

            // Expected output
            String expectedOut = ("Attempting to add task: " + title + "\nDesc: " + description).trim();

            // Assertions
            assertEquals(expectedOut, actualOut, "Console out mismatch");
            // Use assertions that are more resilient to minor formatting differences
            assertTrue(actualErr.contains("Error adding task: Error adding task"), "Console err mismatch - part 1");
            assertTrue(actualErr.contains("some sql error"));
            // Verify DAO call
            verify(tasksDAO, times(1)).addTask(any(Task.class));

            // Verify a proper error message in view
            verify(view).showMessage("Error adding task: Error adding task", MessageType.ERROR);
        }
    }

    @Nested
    @DisplayName("Update Task tests")
    class EditTaskTests {
        /*
         * Test scenarios:
         * 1. Successful task update
         * 2. DB throws an error
         * 3. The task was not found on DB
         *
         * We don't cover the option that the strings are empty because
         * we assume if the title is empty that this function will not run,
         * it being conditioned in updateButtonPressed().
         * */

        TasksViewModel viewModel;
        //Define behavior on these variables
        TasksDAODerby tasksDAO;
        TaskManagerView view;
        //Add latch to notify thread when addTask is finished
        CountDownLatch latch =  new CountDownLatch(1);
        //Console logger control (two variables to control the stream to the terminal)
        ByteArrayOutputStream outContent;
        PrintStream originalOut;
        ByteArrayOutputStream errContent;
        PrintStream originalErr;
        @BeforeEach
        void beforeEach() {
            view = mock(TaskManagerView.class);
            tasksDAO = mock(TasksDAODerby.class);

            viewModel = new TasksViewModel(tasksDAO, view);

            /*
            Since the addTask function runs on a new service, we must await its products
            Create a latch to invoke a notification when the thread finished preforming the function
            */
            doAnswer(invocation -> {
                latch.countDown(); // signal that showMessage was called
                return null;
            }).when(view).showMessage(anyString(), any());

            // Arrange: redirect System.out
            outContent = new ByteArrayOutputStream();
            originalOut = System.out;
            errContent = new ByteArrayOutputStream();
            originalErr = System.err;

            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }
        @AfterEach
        void afterEach(){
            // Reset System.out and System.err
            System.setOut(originalOut);
            System.setErr(originalErr);
            // Reset taskList
            viewModel.getTasksList().get().clear();
        }

        /**
         * Tests the successful update of a task.
         * Verifies that the task is updated in the DAO, the ViewModel's list is updated,
         * and a success message is displayed.
         */
        @Test
        void testUpdateTask_successful() throws Exception {
            int id = 1;
            String title = "Drink food";
            String description = "";

            // Create a task and add it to ViewModel
            Task task = new Task(id, "test1", "test1", new ToDoState());
            viewModel.getAllTasks().add(task);

            // Mock DAO behavior
            when(tasksDAO.getTask(id)).thenReturn(task);
            when(tasksDAO.getTasks()).thenReturn(new ITask[]{task});

            // Call the method under test
            viewModel.updateTask(id, title, description, new ToDoState().next());

            // Wait for the async update to finish (maximum 3 seconds)
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            assertEquals("Attempting to update task ID: " + id + System.lineSeparator(), outContent.toString());
            // Verify DAO update
            verify(tasksDAO, times(1)).updateTask(task);

            // Verify view updates
            verify(view, atLeastOnce()).setTasks(anyList());
            verify(view, times(1)).showMessage(
                    "Task \"" + title + "\" updated successfully!",
                    MessageType.SUCCESS
            );
            // Check that the task in ViewModel is updated
            ITask updatedTask = viewModel.getTasksList().get().stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElse(null);

            assertNotNull(updatedTask, "Updated task should not be null");
            assertEquals(title, updatedTask.getTitle(), "Task title should be updated");
            assertEquals(description, updatedTask.getDescription(), "Task description should be updated");
        }


        /**
         * Tests that the {@link TasksViewModel#updateTask(int, String, String, TaskState)} method
         * handles a database exception gracefully. It verifies that an error message is shown to the user.
         */
        @Test
        void testUpdateTask_failedDB() throws Exception {
            int id = 1;
            String title = "";
            String description = "";
            Task task = new Task(id, "test1", "test1", new ToDoState());

            // Mock DAO behavior: return the task when getTask(id) is called
            when(tasksDAO.getTask(id)).thenReturn(task);

            // Update task fields
            task.setTitle(title);
            task.setDescription(description);
            task.setState(task.getState().next());

            // Mock DAO to throw exception on update
            doThrow(new TasksDAOException("Error updating task", new SQLException("some sql error")))
                    .when(tasksDAO).updateTask(task);

            // Setup latch to wait for async showMessage
            doAnswer(invocation -> {
                latch.countDown(); // Signal that showMessage was called
                return null;
            }).when(view).showMessage(anyString(), any());

            // Redirect System.out and System.err to capture console output
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));

            try {
                // Invoke the method under test
                viewModel.updateTask(id, title, description, new ToDoState().next());

                // Wait for async updates to finish
                boolean finished = latch.await(3, TimeUnit.SECONDS);
                assertTrue(finished, "Timeout waiting for async update");

                // Verify console output
                assertEquals("Attempting to update task ID: " + id + System.lineSeparator(), outContent.toString(), "Console out mismatch");
                assertEquals("Error updating task: Error updating task" + System.lineSeparator(), errContent.toString(), "Console err mismatch");

                // Verify DAO update was attempted exactly once
                verify(tasksDAO, times(1)).updateTask(task);

                // Verify the proper error pop-up in view
                verify(view).showMessage(contains("Error updating task"), eq(MessageType.ERROR));
            } finally {
                // Restore original System.out and System.err
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        }


        /**
         * Tests that the {@link TasksViewModel#updateTask(int, String, String, TaskState)} method
         * handles the case where the task is not found in the database.
         */
        @Test
        void testUpdateTask_failedNotFound() throws Exception {

            int id = -1;

            //Define DB behavior
            when(tasksDAO.getTask(id)).thenReturn(null);

            //Invoke test action
            viewModel.updateTask(id, "test1", "test1", new ToDoState());
            boolean _ = latch.await(3, TimeUnit.SECONDS);
            //Ensure print to  console correctly
            assertEquals("Task not found for update. ID: " + id + System.lineSeparator(),  errContent.toString());

            //Ensure proper error pop-up
            verify(view).showMessage("Error updating task: Task not found.", MessageType.ERROR);
        }

    }

    @Nested
    @DisplayName("Move Task State Up tests")
    class MoveTaskStateUpTests {
        TasksViewModel viewModel;
        // Define behavior on these variables
        TasksDAODerby tasksDAO;
        TaskManagerView view;
        // Add latch to notify thread when async function is finished
        CountDownLatch latch = new CountDownLatch(1);
        // Console logger control
        ByteArrayOutputStream outContent;
        PrintStream originalOut;
        ByteArrayOutputStream errContent;
        PrintStream originalErr;

        @BeforeEach
        void beforeEach() {
            view = mock(TaskManagerView.class);
            tasksDAO = mock(TasksDAODerby.class);

            viewModel = new TasksViewModel(tasksDAO, view);

            // latch is released when showMessage is called
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(view).showMessage(anyString(), any());

            // Redirect System.out and System.err
            outContent = new ByteArrayOutputStream();
            originalOut = System.out;
            errContent = new ByteArrayOutputStream();
            originalErr = System.err;

            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        @AfterEach
        void afterEach() {
            // Reset streams
            System.setOut(originalOut);
            System.setErr(originalErr);
            // Reset taskList
            viewModel.getTasksList().get().clear();
        }

        /**
         * Tests the successful state transition of a task to the next state.
         * Verifies that the task's state is updated in the DAO, the ViewModel's list,
         * and that the view is notified of the change.
         */
        @Test
        void testMoveTaskStateUp_successful() throws Exception {
            int id = 1;

            // Create a task with an initial state
            Task task = new Task(id, "Test Task", "desc", new ToDoState());

            // Mock DAO methods
            doReturn(task).when(tasksDAO).getTask(id);           // Return the task when getTask(id) is called
            ITask[] tasksArray = new ITask[]{ task };
            doReturn(tasksArray).when(tasksDAO).getTasks();     // Return all tasks when getTasks() is called

            // Initialize the ViewModel's observable collection with a modifiable list
            viewModel.getTasksList().setValue(new ArrayList<>(Arrays.asList(task)));

            // Setup CountDownLatch for asynchronous updates
            CountDownLatch latch = new CountDownLatch(1);
            doAnswer(invocation -> {
                latch.countDown(); // Countdown when setTasks is called
                return null;
            }).when(view).setTasks(any());

            // Call the method under test
            viewModel.moveTaskStateUp(id);


            // Wait for the async update to complete (max 3 seconds)
            boolean finished = latch.await(3, TimeUnit.SECONDS);
            assertTrue(finished, "Timeout waiting for async update");

            // Verify the DAO updated the task
            verify(tasksDAO, times(1)).updateTask(task);

            // Verify the task is present in the ViewModel's observable collection
            ITask updatedTask = viewModel.getTasksList().get().stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElse(null);

            assertNotNull(updatedTask, "Updated task should not be null");

            // Verify the task state has advanced correctly
            assertEquals(new ToDoState().next(), updatedTask.getState(), "Task state should be updated");

            // Verify the view shows the success message
            verify(view).showMessage(
                    "Task state updated to " + updatedTask.getState().getDisplayName() + ".",
                    MessageType.SUCCESS
            );
        }


        /**
         * Tests that the {@link TasksViewModel#moveTaskStateUp(int)} method handles the
         * case where the task is not found in the database.
         */
        @Test
        void testMoveTaskStateUp_taskNotFound() throws Exception {
            int id = 999;

            // Mock DAO returning null
            when(tasksDAO.getTask(id)).thenReturn(null);

            // Invoke
            viewModel.moveTaskStateUp(id);
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            // Ensure DAO update never called
            verify(tasksDAO, never()).updateTask(any());

            // Ensure an error message shown
            verify(view).showMessage("Error: Task with ID " + id + " not found.", MessageType.ERROR);
        }
    }

    @Nested
    @DisplayName("Delete Task tests")
    class DeleteTaskTests {
        TasksViewModel viewModel;
        // Define behavior on these variables
        TasksDAODerby tasksDAO;
        TaskManagerView view;
        // Add latch to notify thread when async function is finished
        CountDownLatch latch = new CountDownLatch(1);
        // Console logger control
        ByteArrayOutputStream outContent;
        PrintStream originalOut;
        ByteArrayOutputStream errContent;
        PrintStream originalErr;

        @BeforeEach
        void beforeEach() {
            view = mock(TaskManagerView.class);
            tasksDAO = mock(TasksDAODerby.class);

            viewModel = new TasksViewModel(tasksDAO, view);

            // latch is released when showMessage is called
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(view).showMessage(anyString(), any());

            // Redirect System.out and System.err
            outContent = new ByteArrayOutputStream();
            originalOut = System.out;
            errContent = new ByteArrayOutputStream();
            originalErr = System.err;

            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        @AfterEach
        void afterEach() {
            // Reset streams
            System.setOut(originalOut);
            System.setErr(originalErr);
            // Reset taskList
            viewModel.getTasksList().get().clear();
        }

        /**
         * Tests the successful state transition of a task to the next state.
         * Verifies that the task's state is updated in the DAO, the ViewModel's list,
         * and that the view is notified of the change.
         */
        @Test
        void testMoveTaskStateUp_successful() throws Exception {
            int id = 1;

            // Create the task with the initial state
            Task task = new Task(id, "Test Task", "desc", new ToDoState());

            // Mock DAO methods
            doReturn(task).when(tasksDAO).getTask(id);  // Return the task when getTask(id) is called
            Task[] tasksArray = new Task[]{ task };
            doReturn(tasksArray).when(tasksDAO).getTasks();  // Return all tasks when getTasks() is called

            // Synchronize the ObservableCollection in the ViewModel with the array
            viewModel.getTasksList().setValue(new ArrayList<>(Arrays.asList(tasksArray)));

            // Create a latch to wait for asynchronous updates
            CountDownLatch latch = new CountDownLatch(1);
            doAnswer(invocation -> {
                latch.countDown();  // Countdown when setTasks is called
                return null;
            }).when(view).setTasks(any());

            // Invoke the method under test
            viewModel.moveTaskStateUp(id);

            // Wait for the asynchronous update to finish (maximum 3 seconds)
            boolean finished = latch.await(3, TimeUnit.SECONDS);
            assertTrue(finished, "Timeout waiting for async update");

            // Verify that the DAO updated the task
            verify(tasksDAO, times(1)).updateTask(task);

            // Check that the task state has actually changed
            assertEquals(new ToDoState().next(), task.getState(), "Task state should be updated");

            // Verify that the view displays the success message
            verify(view).showMessage(
                    "Task state updated to " + task.getState().getDisplayName() + ".",
                    MessageType.SUCCESS
            );
        }

        /**
         * Tests that the {@link TasksViewModel#deleteTask(int)} method handles a
         * database exception gracefully.
         */
        @Test
        void testDeleteTask_failedDB() throws Exception {
            int id = 2;
            Task task = new Task(id, "Fail Delete", "desc", new ToDoState());
            viewModel.getAllTasks().add(task);
            viewModel.getTasksList().appendValue(task);

            // Mock DAO to throw error
            doThrow(new TasksDAOException("Database error", new SQLException("delete failed")))
                    .when(tasksDAO).deleteTask(id);

            // Invoke
            viewModel.deleteTask(id);
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            // Ensure DAO delete called
            verify(tasksDAO, times(1)).deleteTask(id);

            // Ensure a task was NOT removed from in-memory lists
            assertTrue(viewModel.getAllTasks().stream().anyMatch(t -> t.getId() == id));
            assertTrue(viewModel.getTasksList().get().stream().anyMatch(t -> t.getId() == id));

            // Ensure an error message shown
            verify(view).showMessage("Error deleting task: Database error", MessageType.ERROR);

            // Ensure error logged to console
            assertEquals("Error deleting task: Database error" + System.lineSeparator(), errContent.toString());
        }
    }

    @Nested
    @DisplayName("Generate Report tests")
    class GenerateReportTests {
        TasksViewModel viewModel;
        TasksDAODerby tasksDAO;
        TaskManagerView view;
        CountDownLatch latch;
        ByteArrayOutputStream outContent;
        PrintStream originalOut;
        ByteArrayOutputStream errContent;
        PrintStream originalErr;
        IReportExporter mockExporter;
        IReportExporter originalExporter;

        @BeforeEach
        void beforeEach() {
            view = mock(TaskManagerView.class);
            tasksDAO = mock(TasksDAODerby.class);

            viewModel = new TasksViewModel(tasksDAO, view);

            // Add a latch for async completion
            latch = new CountDownLatch(1);
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(view).showMessage(anyString(), any());

            // Capture console
            outContent = new ByteArrayOutputStream();
            originalOut = System.out;
            errContent = new ByteArrayOutputStream();
            originalErr = System.err;
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));

            // Add a mock exporter
            mockExporter = mock(IReportExporter.class);
            originalExporter = viewModel.getExporters().get("CSV");
            viewModel.getExporters().put("CSV", mockExporter);
        }

        @AfterEach
        void afterEach() {
            System.setOut(originalOut);
            System.setErr(originalErr);
            viewModel.getTasksList().get().clear();
            viewModel.getAllTasks().clear();
            viewModel.getExporters().put("CSV", originalExporter);
        }

        /**
         * Tests the successful generation of a CSV report.
         * It verifies that the exporter is called with the correct data and filename,
         * and that a success message is displayed.
         */
        @Test
        void testGenerateReport_successfulCSV() throws Exception {
            Task task = new Task(1, "Test", "Desc", new ToDoState());
            viewModel.getAllTasks().add(task);

            // Mock exporter to succeed
            doNothing().when(mockExporter).export(any(ReportData.class), anyString());

            // Invoke
            viewModel.generateReport("CSV");
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            // Verify exporter was called
            verify(mockExporter, times(1)).export(any(ReportData.class), eq("report.csv"));

            // Verify success message
            verify(view).showMessage("Report generated successfully: report.csv", MessageType.SUCCESS);

            // Ensure no errors in console
            assertEquals("", errContent.toString());
        }

        /**
         * Tests that the {@link TasksViewModel#generateReport(String)} method
         * handles an unsupported format correctly. It verifies that a warning message
         * is shown and no exporter is called.
         */
        @Test
        void testGenerateReport_unsupportedFormat() throws Exception {
            // Invoke with unsupported format
            viewModel.generateReport("XML");
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            // Verify the exporter ISN'T called
            verify(mockExporter, never()).export(any(), anyString());

            // Verify warning message
            verify(view).showMessage("Unsupported report format: XML", MessageType.WARNING);

            // Verify console error
            assertEquals("Unsupported report format: XML" + System.lineSeparator(), errContent.toString());
        }

    }


    @Nested
    @DisplayName("Filter Task tests")
    class FilterTaskTests {

        TasksViewModel viewModel;
        TasksDAODerby tasksDAO;
        TaskManagerView view;

        @BeforeEach
        void beforeEach() {
            view = mock(TaskManagerView.class);
            tasksDAO = mock(TasksDAODerby.class);
            viewModel = new TasksViewModel(tasksDAO, view);

            // Seed some tasks into allTasks
            viewModel.getAllTasks().addAll(List.of(
                    new Task(1, "Buy milk", "From the store", new ToDoState()),
                    new Task(2, "Finish report", "For manager", new InProgressState(new ToDoState())),
                    new Task(3, "Workout", "Gym session", new CompletedState(new InProgressState(new ToDoState()))),
                    new Task(4, "Read book", "Novel", new ToDoState())
            ));
        }

        @AfterEach
        void afterEach() {
            viewModel.getTasksList().get().clear();
            viewModel.getAllTasks().clear();
        }

        /**
         * Tests filtering tasks by state.
         * It verifies that only tasks with the specified state remain in the filtered list.
         */
        @Test
        void testFilterTasks_byState() {
            // Act: filter only To-Do tasks
            viewModel.filterTasks("To Do", "", "", "");

            // Assert: only tasks with the To-Do state should remain
            List<ITask> filtered = viewModel.getTasksList().get();
            assertEquals(2, filtered.size());
            assertTrue(filtered.stream().allMatch(t -> t.getState() instanceof ToDoState));

            verify(view, times(1)).setTasks(ArgumentMatchers.<List<ITask>>any());
        }

        /**
         * Tests filtering tasks by title and description.
         * It verifies that only tasks matching both criteria remain.
         */
        @Test
        void testFilterTasks_byTitleAndDescription() {
            // Act: filter by title = "Finish" and description = "manager"
            viewModel.filterTasks("All", "Finish", "manager", "");

            // Assert: only task 2 matches
            List<ITask> filtered = viewModel.getTasksList().get();
            assertEquals(1, filtered.size());
            assertEquals(2, filtered.getFirst().getId());

            verify(view, times(1)).setTasks(ArgumentMatchers.<List<ITask>>any());
        }

        @Test

        void testFilterTasks_byId_validAndInvalid() {
            // Act: valid ID filter
            viewModel.filterTasks("All", "", "", "3");
            // Assert: only task 3 should be returned
            List<ITask> filteredValid = viewModel.getTasksList().get();
            assertEquals(1, filteredValid.size());
            assertEquals(3, filteredValid.getFirst().getId());
            // Act: invalid ID filter (non-numeric)
            viewModel.filterTasks("All", "", "", "not-a-number");
            // Assert: no crash, tasksList should still contain all tasks
            List<ITask> filteredInvalid = viewModel.getTasksList().get();
            assertEquals(4, filteredInvalid.size()); // unchanged
        }
    }
}