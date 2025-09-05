package viewmodel.strategy;

import model.task.ITask;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortByCreationDateStrategy implements SortingStrategy {
    @Override
    public void sort(List<ITask> tasks) {
        // Sort in ascending order (oldest to newest)
        Collections.sort(tasks, Comparator.comparing(ITask::getCreationDate));
    }
}
