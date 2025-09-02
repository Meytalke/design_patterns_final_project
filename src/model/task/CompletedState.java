package model.task;

public class CompletedState implements TaskState {
    @Override
    public String getDisplayName() {
        return "Completed";
    }

    @Override
    public TaskState next() {
        return this;
    }

    @Override
    public TaskState previous() {
        return new InProgressState();
    }
}
