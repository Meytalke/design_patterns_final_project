package model.report.external;

import model.task.ITask;
import model.task.Task;

import java.util.List;

/**
 * A simple text-based reporter for generating console output reports.
 * This class is responsible for printing a formatted report to the console
 * that summarizes the counts of completed tasks, tasks in progress, and tasks to do.
 */
public class SimpleTextReporter {
    public void generateTextReport(long completed, long inProgress, long todo, List<ITask> completedBucket, List<ITask> inProgressBucket, List<ITask>  todoBucket) {
        System.out.println("--- Report ---");
        System.out.println("Completed: " + completed);
        System.out.println("In Progress: " + inProgress);
        System.out.println("To Do: " + todo);
        System.out.println("--- ToDo Bucket ---");
        for(ITask task: todoBucket) {
            System.out.println(task);
        }
        System.out.println("--- InProgress Bucket ---");
        for(ITask task: inProgressBucket) {
            System.out.println(task);
        }
        System.out.println("--- Completed Bucket ---");
        for(ITask task: completedBucket) {
            System.out.println(task);
        }
        System.out.println("--- End of Report ---");
    }
}
