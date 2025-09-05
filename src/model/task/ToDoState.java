package model.task;

public class ToDoState implements TaskState {
    private final InProgressState inProgressState = new InProgressState(this);

    @Override
    public String getDisplayName() {
        return "To Do";
    }

    @Override
    public TaskState next() {return getInProgressState();}

    @Override
    public TaskState previous() {return this;}

    public TaskState getInProgressState() {return inProgressState;}
}
