package model.report;

import model.task.ITask;

public class ReportVisitor implements TaskVisitor {
    private long completedTasks = 0;
    private long inProgressTasks = 0;
    private long todoTasks = 0;

    @Override
    public void visit(ITask task) {
        // Pattern Matching is used here to match the task's state.
        switch (task.getState()) {
            case COMPLETED -> completedTasks++;
            case IN_PROGRESS -> inProgressTasks++;
            case TO_DO -> todoTasks++;
        }
    }

    public ReportData getReport() {
        return new ReportData(completedTasks, inProgressTasks, todoTasks);
    }
}
