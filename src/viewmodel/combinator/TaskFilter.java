package viewmodel.combinator;

import model.task.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@FunctionalInterface
public interface TaskFilter {

    List<ITask> filter(List<ITask> tasks);

    static TaskFilter byId(int id) {
        return tasks -> tasks.stream()
                .filter(task -> task.getId() == id)
                .collect(Collectors.toList());
    }

    static TaskFilter byTitle(String searchTerm) {
        String finalSearchTerm = searchTerm.toLowerCase();
        return tasks -> tasks.stream()
                .filter(task -> task.getTitle().toLowerCase().contains(finalSearchTerm))
                .collect(Collectors.toList());
    }

    static TaskFilter byDescription(String searchTerm) {
        String finalSearchTerm = searchTerm.toLowerCase();
        return tasks -> tasks.stream()
                .filter(task -> task.getDescription().toLowerCase().contains(finalSearchTerm))
                .collect(Collectors.toList());
    }

    static TaskFilter byState(String state) {
        if (state == null || "All".equalsIgnoreCase(state)) {
            return tasks -> tasks;
        }

        TaskState taskState;
        switch (state) {
            //Use the linking of object instances to save in memory (at most 3 states instances)
            case "To Do" -> taskState = new ToDoState();
            case "In Progress" -> taskState = new InProgressState(new ToDoState());
            case "Completed" -> taskState = new CompletedState(new InProgressState(new ToDoState()));
            default -> throw new IllegalArgumentException("Unknown state: " + state);
        }

        return tasks -> tasks.stream()
                .filter(task -> task.getState().getDisplayName().equals(taskState.getDisplayName()))
                .collect(Collectors.toList());
    }

    default TaskFilter and(TaskFilter other) {
        return tasks -> other.filter(this.filter(tasks));
    }

    default TaskFilter or(TaskFilter other) {
        return tasks -> {
            Set<ITask> combinedResults = new HashSet<>();
            combinedResults.addAll(this.filter(tasks));
            combinedResults.addAll(other.filter(tasks));
            return new ArrayList<>(combinedResults);
        };
    }
}