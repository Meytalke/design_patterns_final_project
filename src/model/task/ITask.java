package model.task;

import model.report.TaskVisitor;

public interface ITask {
    int getId();
    String getTitle();
    String getDescription();
    ITaskState getState();
    void accept(TaskVisitor visitor);
}