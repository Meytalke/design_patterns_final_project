package model.report;

import model.task.*;

public class ReportVisitor implements TaskVisitor {
    private long completedTasks = 0;
    private long inProgressTasks = 0;
    private long todoTasks = 0;

    @Override
    public void visit(ITask task) {
        ITaskState state = task.getState();
        switch (state) {
            case CompletedState _ -> completedTasks++;
            case InProgressState _ -> inProgressTasks++;
            case ToDoState _ -> todoTasks++;
            case null, default -> throw new IllegalStateException("Unknown task state: " + state);
        }
    }

    public ReportData getReport() {
        return new ReportData(completedTasks, inProgressTasks, todoTasks);
    }
}
