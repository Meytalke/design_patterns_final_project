package model.task;

public class InProgressState implements TaskState {
    private final TaskState toDoState;
    private final TaskState completedState = new CompletedState(this);

    public InProgressState(TaskState toDoState) {
        this.toDoState = toDoState;
    }

    @Override
    public String getDisplayName() {
        return "In Progress";
    }

    @Override
    public TaskState next() {return getCompletedState();}

    @Override
    public TaskState previous() {return getToDoState();}

    public TaskState getToDoState() {return toDoState;}

    public TaskState getCompletedState() {return completedState;}
}
