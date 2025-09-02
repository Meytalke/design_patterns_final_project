package model.task;

public class InProgressState implements ITaskState {

    private final ITaskState toDoState;
    private final ITaskState completedState = new CompletedState(this);

    public InProgressState(ITaskState toDoState) {
        this.toDoState = toDoState;
    }

    @Override
    public String getDisplayName() {
        return "In Progress";
    }

    @Override
    public ITaskState next() {
        return getCompletedState();
    }

    @Override
    public ITaskState previous() {return getToDoState();}

    public ITaskState getToDoState() {return toDoState;}

    public ITaskState getCompletedState() {return completedState;}
}
