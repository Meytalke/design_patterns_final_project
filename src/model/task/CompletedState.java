package model.task;

public class CompletedState implements TaskState {
    private final TaskState inProgressState;

    public CompletedState(TaskState inProgressState) {
        this.inProgressState = inProgressState;
    }
    @Override
    public String getDisplayName() {
        return "Completed";
    }

    @Override
    public TaskState next() {return this;}

    @Override
    public TaskState previous() {return getInProgressState();}

    public TaskState getInProgressState() {return  inProgressState;}
}
