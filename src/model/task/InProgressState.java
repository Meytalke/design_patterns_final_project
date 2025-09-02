package model.task;

public class InProgressState implements TaskState {
    @Override
    public String getDisplayName() {
        return "In Progress";
    }

    @Override
    public TaskState next() {
        return new CompletedState();
    }

    @Override
    public TaskState previous() {
        return new ToDoState();
    }
}
