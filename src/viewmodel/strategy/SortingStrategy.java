package viewmodel.strategy;

import model.task.ITask;

import java.util.List;

public interface SortingStrategy {
    void sort(List<ITask> tasks);
}
