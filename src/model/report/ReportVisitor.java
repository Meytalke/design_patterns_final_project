package model.report;

import model.task.*;

public class ReportVisitor implements TaskVisitor {
    private long completedTasks = 0;
    private long inProgressTasks = 0;
    private long todoTasks = 0;

//    @Override
//    public void visit(ITask task) {
//        String stateName = task.getState().getDisplayName();
//        switch (stateName) {
//            case "Completed" -> completedTasks++;
//            case "In Progress" -> inProgressTasks++;
//            case "To Do" -> todoTasks++;
//            default -> throw new IllegalStateException("Unknown task state: " + stateName);
//        }
//    }

    @Override
    public void visit(ITask task) {
        TaskState state = task.getState();
        if (state instanceof CompletedState) {
            completedTasks++;
        } else if (state instanceof InProgressState) {
            inProgressTasks++;
        } else if (state instanceof ToDoState) {
            todoTasks++;
        } else {
            throw new IllegalStateException("Unknown task state: " + state);
        }
    }

    public ReportData getReport() {
        return new ReportData(completedTasks, inProgressTasks, todoTasks);
    }
}
