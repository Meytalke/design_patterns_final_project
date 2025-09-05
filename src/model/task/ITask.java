package model.task;

import model.report.TaskVisitor;

import java.util.Date;

public interface ITask {
    int getId();
    String getTitle();
    String getDescription();
    TaskState getState();
    TaskPriority getPriority();
    Date getCreationDate();
    void accept(TaskVisitor visitor);
}