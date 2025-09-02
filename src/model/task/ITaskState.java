package model.task;

public interface ITaskState {
    String getDisplayName();
    ITaskState next();
    ITaskState previous();
}
