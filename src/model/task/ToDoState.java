package model.task;

public class ToDoState implements TaskState {
    @Override
    public String getDisplayName() {
        return "To Do";
    }

    @Override
    public TaskState next() {
        return new InProgressState();
    }

    @Override
    public TaskState previous() {
        return this;
    }
}
