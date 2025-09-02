package model.task;

public class ToDoState implements ITaskState {

    private final InProgressState inProgressState = new InProgressState(this);

    @Override
    public String getDisplayName() {
        return "To Do";
    }

    @Override
    public ITaskState next() {
        return getInProgressState();
    }

    @Override
    public ITaskState previous() {
        return this;
    }

    public ITaskState getInProgressState() {return inProgressState;}
}
