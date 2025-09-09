// TasksViewModelTest.java
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
}