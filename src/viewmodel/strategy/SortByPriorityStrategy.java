package viewmodel.strategy;

import model.task.ITask;

import java.util.Comparator;
import java.util.List;

public class SortByPriorityStrategy implements SortingStrategy {
    @Override
    public void sort(List<ITask> tasks) {
        // Sort in descending order (HIGH to LOW)
        tasks.sort(Comparator.comparing(ITask::getPriority).reversed());
    }
}
