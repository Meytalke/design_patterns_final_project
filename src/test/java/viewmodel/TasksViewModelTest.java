// TasksViewModelTest.java
package il.ac.hit.project.test.viewmodel;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.dao.TasksDAODerby;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.report.IReportExporter;
import il.ac.hit.project.main.model.report.ReportData;
import il.ac.hit.project.main.model.task.*;
import il.ac.hit.project.main.view.TaskManagerView;
import il.ac.hit.project.main.viewmodel.IViewModel;
import org.junit.jupiter.api.*;

import il.ac.hit.project.main.view.IView;
import il.ac.hit.project.main.view.MessageType;
import il.ac.hit.project.main.viewmodel.TasksViewModel;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.stream.Stream;

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
            //Ensure tasksList was updated and listeners were notified
            //Verify UI update
            verify(view, times(1)).setTasks(ArgumentMatchers.<List<ITask>>any());
            ITask checkTask = null;
            //Search for task in tasksList
            checkTask = viewModel.getTasksList().get().stream().filter((t)->t.getTitle().equals(title)).toList().getFirst();
            assertNotNull(checkTask);
            //Ensure view was told to show a success message
            verify(view).showMessage("Task \"" + title + "\" added successfully!", MessageType.SUCCESS);
        }

        @Test
        void testAddTask_failed() throws Exception {

            String title = "";
            String description = "";

            //Change mock DAO behavior to throw TasksDAOException
            doThrow(new TasksDAOException("Error adding task", new SQLException("some sql error")))
                    .when(tasksDAO).addTask(any(Task.class));

            //Invoke test action
            viewModel.addTask(title,description);
            boolean _ = latch.await(3, TimeUnit.SECONDS);


            //Ensure print to console correctly
            assertEquals("Attempting to add task: " + title + "\nDesc: " + description + System.lineSeparator() , outContent.toString());
            assertEquals("Error adding task: " + "Error adding task"+ "\nCause: "+ "java.sql.SQLException: some sql error" + System.lineSeparator(), errContent.toString());


            //Ensure model was called to addTask
            verify(tasksDAO, times(1)).addTask(any(Task.class));

            //Ensure proper error pop-up
            verify(view).showMessage("Error adding task: " + "Error adding task",MessageType.ERROR);

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

        @Test
        void testUpdateTask_successful() throws Exception {

            int id = 1;
            String title = "Drink food";
            String description = "";
            //Assume we have the task in memory
            Task task = new Task(id, "test1", "test1", new ToDoState());
            viewModel.getAllTasks().add(task);
            //Define DB behavior
            when(tasksDAO.getTask(id)).thenReturn(task);

            //Invoke test action
            viewModel.updateTask(id, title, description, new ToDoState().next() );
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            //Ensure print to console correctly
            assertEquals("Attempting to update task ID: " + id + System.lineSeparator() + "Updating task list" + System.lineSeparator(), outContent.toString());
            //Ensure model was called to updateTask
            verify(tasksDAO, times(1)).updateTask(task);
            //Ensure tasksList was updated and listeners were notified
            //Verify UI update
            verify(view, times(1)).setTasks(ArgumentMatchers.<List<ITask>>any());
            //Search for task in tasksList
            ITask checkTask = viewModel.getTasksList().get().stream()
                    .filter(t-> t.getId() == id)
                    .findFirst()
                    .orElse(null);

            assertNotNull(checkTask);
            //Ensure view was told to show a success message
            verify(view).showMessage("Task \"" + title + "\" updated successfully!", MessageType.SUCCESS);
        }

        @Test
        void testUpdateTask_failedDB() throws Exception {

            int id = 1;
            String title = "";
            String description = "";
            Task task = new Task(id, "test1", "test1", new ToDoState());

            //Define DB behavior
            when(tasksDAO.getTask(id)).thenReturn(task);
            task.setTitle(title);
            task.setDescription(description);
            task.setState(task.getState().next());
            //Change mock DAO behavior to throw TasksDAOException
            doThrow(new TasksDAOException("Error updating task", new SQLException("some sql error")))
                    .when(tasksDAO).updateTask(task);

            //Invoke test action
            viewModel.updateTask(id,title,description,  new ToDoState().next());
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            //Ensure print to console correctly
            assertEquals("Attempting to update task ID: " + id + System.lineSeparator() , outContent.toString());
            assertEquals("Error updating task: " + "Error updating task"+ "\nCause: "+ "java.sql.SQLException: some sql error" + System.lineSeparator(), errContent.toString());

            //Ensure model was called to addTask
            verify(tasksDAO, times(1)).updateTask(task);

            //Ensure proper error pop-up
            verify(view).showMessage("Error updating task: " + "Error updating task",MessageType.ERROR);
        }
        @Test
        void testUpdateTask_failedNotFound() throws Exception {

            int id = -1;

            //Define DB behavior
            when(tasksDAO.getTask(id)).thenReturn(null);

            //Invoke test action
            viewModel.updateTask(id, "test1", "test1", new ToDoState());
            boolean _ = latch.await(3, TimeUnit.SECONDS);
            //Ensure print to  to console correctly
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

        @Test
        void testMoveTaskStateUp_successful() throws Exception {
            int id = 1;
            Task task = new Task(id, "Test Task", "desc", new ToDoState().next());
            viewModel.getAllTasks().add(task);

            // Mock DAO returning the task
            when(tasksDAO.getTask(id)).thenReturn(task);

            // Invoke
            viewModel.moveTaskStateUp(id);
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            // Ensure DAO update called
            verify(tasksDAO, times(1)).updateTask(task);

            // Ensure in-memory task is updated
            ITask updatedTask = viewModel.getTasksList().get().stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElse(null);

            assertNotNull(updatedTask);
            assertEquals(task.getState(), updatedTask.getState());

            // Ensure success message shown
            verify(view).showMessage("Task state updated to " + task.getState().getDisplayName() + ".", MessageType.SUCCESS);
        }

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

            // Ensure error message shown
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

        @Test
        void testDeleteTask_successful() throws Exception {
            int id = 1;
            Task task = new Task(id, "Delete Me", "desc", new ToDoState());
            viewModel.getAllTasks().add(task);
            viewModel.getTasksList().appendValue(task);

            // Mock DAO successful deletion
            doNothing().when(tasksDAO).deleteTask(id);

            // Invoke
            viewModel.deleteTask(id);
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            // Ensure DAO delete called
            verify(tasksDAO, times(1)).deleteTask(id);

            // Ensure task removed from in-memory lists
            assertTrue(viewModel.getAllTasks().stream().noneMatch(t -> t.getId() == id));
            assertTrue(viewModel.getTasksList().get().stream().noneMatch(t -> t.getId() == id));

            // Ensure success message shown
            verify(view).showMessage("Task with ID " + id + " deleted successfully.", MessageType.SUCCESS);

            // Ensure console log is empty (since no error expected)
            assertEquals("", errContent.toString());
        }

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

            // Ensure task was NOT removed from in-memory lists
            assertTrue(viewModel.getAllTasks().stream().anyMatch(t -> t.getId() == id));
            assertTrue(viewModel.getTasksList().get().stream().anyMatch(t -> t.getId() == id));

            // Ensure error message shown
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

        @Test
        void testGenerateReport_unsupportedFormat() throws Exception {
            // Invoke with unsupported format
            viewModel.generateReport("XML");
            boolean _ = latch.await(3, TimeUnit.SECONDS);

            // Verify exporter NOT called
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

        @Test
        void testFilterTasks_byState() {
            // Act: filter only To-Do tasks
            viewModel.filterTasks("To Do", "", "", "");

            // Assert: only tasks with To-Do state should remain
            List<ITask> filtered = viewModel.getTasksList().get();
            assertEquals(2, filtered.size());
            assertTrue(filtered.stream().allMatch(t -> t.getState() instanceof ToDoState));

            verify(view, times(1)).setTasks(ArgumentMatchers.<List<ITask>>any());
        }

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