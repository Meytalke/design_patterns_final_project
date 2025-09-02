package model.task;

public interface TaskState {
    String getDisplayName();
    TaskState next();
    TaskState previous();
}
