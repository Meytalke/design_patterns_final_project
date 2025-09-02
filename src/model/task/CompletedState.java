package model.task;

public class CompletedState implements ITaskState {

    private final ITaskState inProgressState;

    public CompletedState(ITaskState inProgressState) {
        this.inProgressState = inProgressState;
    }


    @Override
    public String getDisplayName() {
        return "Completed";
    }

    @Override
    public ITaskState next() {
        return this;
    }

    @Override
    public ITaskState previous() {
        return getInProgressState();
    }

    public ITaskState getInProgressState() {return  inProgressState;}
}
