package viewmodel.strategy;

import model.task.ITask;

import java.util.Comparator;
import java.util.List;

public class SortByCreationDateStrategy implements SortingStrategy {
    @Override
    public void sort(List<ITask> tasks) {
        // Sort in ascending order (oldest to newest)
        tasks.sort(Comparator.comparing(ITask::getCreationDate));
    }
}
