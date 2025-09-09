package il.ac.hit.project.test.viewModel;

import il.ac.hit.project.main.model.dao.ITasksDAO;
import il.ac.hit.project.main.model.dao.TasksDAOException;
import il.ac.hit.project.main.model.task.ITask;
import il.ac.hit.project.main.model.task.Task;
import il.ac.hit.project.main.model.task.ToDoState;
import org.junit.jupiter.api.Test;
import il.ac.hit.project.main.view.IView;
import il.ac.hit.project.main.view.MessageType;
import il.ac.hit.project.main.viewmodel.TasksViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TasksViewModel.
 * <p>
 * This class verifies the behavior of the ViewModel's core functionalities,
 * particularly its interaction with the data layer (ITasksDAO) in asynchronous
 * scenarios.
 */
class TasksViewModelTest {

    /**
     * Tests that the {@code loadTasks} method successfully loads tasks from the DAO.
     * It verifies that the ViewModel's internal lists are populated correctly and
     * that the DAO's getTasks method is called.
     *
     * @throws Exception if an unexpected error occurs during the test.
     */
    @Test
    void testLoadTasksSuccess() throws Exception {
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
     * Tests that the {@code loadTasks} method correctly handles exceptions thrown by the DAO.
     * It verifies that an error message is shown to the view and the internal task list remains empty.
     *
     * @throws Exception if an unexpected error occurs during the test.
     */
    @Test
    void testLoadTasksHandlesException() throws Exception {
        // Arrange
        ITasksDAO mockTasksDAO = mock(ITasksDAO.class);
        IView mockView = mock(IView.class);
        ExecutorService mockExecutorService = mock(ExecutorService.class);

        when(mockTasksDAO.getTasks()).thenThrow(new TasksDAOException("Test Exception"));

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
        verify(mockView, times(1)).showMessage(anyString(), eq(MessageType.ERROR));
        assertTrue(viewModel.getAllTasks().isEmpty());
        verify(mockTasksDAO, times(1)).getTasks();
    }

    /**
     * Tests that the {@code loadTasks} method correctly updates the observable task list.
     * It verifies that the ViewModel's public task list property is updated and
     * that the view's {@code setTasks} method is called.
     *
     * @throws Exception if an unexpected error occurs during the test.
     */
    @Test
    void testLoadTasksUpdatesTaskList() throws Exception {
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
        verify(mockView, times(1)).setTasks(anyList());
    }
}