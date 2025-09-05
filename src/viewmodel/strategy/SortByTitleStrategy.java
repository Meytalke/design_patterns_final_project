package viewmodel.strategy;

import model.task.ITask;

import java.util.Comparator;
import java.util.List;

public class SortByTitleStrategy implements SortingStrategy {
    @Override
    public void sort(List<ITask> tasks) {
        tasks.sort(Comparator.comparing(ITask::getTitle));
    }
}
