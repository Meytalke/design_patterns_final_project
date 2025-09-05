package model.report;

import model.task.*;

public class ReportVisitor implements TaskVisitor {
    private long completedTasks = 0;
    private long inProgressTasks = 0;
    private long todoTasks = 0;

    @Override
    public void visit(ITask task) {
        TaskState state = task.getState();
        switch (state) {
            case CompletedState completedState -> completedTasks++;
            case InProgressState inProgressState -> inProgressTasks++;
            case ToDoState toDoState -> todoTasks++;
            case null, default -> throw new IllegalStateException("Unknown task state: " + state);
        }
    }

    public ReportData getReport() {
        return new ReportData(completedTasks, inProgressTasks, todoTasks);
    }
}
