package model.report;

import model.task.ITask;

public interface TaskVisitor {
    void visit(ITask task);
}
