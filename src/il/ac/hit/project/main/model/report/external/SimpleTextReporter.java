package il.ac.hit.project.main.model.report.external;

import il.ac.hit.project.main.model.task.ITask;

import java.util.List;

/**
 * A simple text-based reporter for generating console output reports.
 * This class is responsible for printing a formatted report to the console
 * that summarizes the counts of completed tasks, tasks in progress, and tasks to do.
 */
public class SimpleTextReporter {
    /**
     * Generates a text-based report and prints it to the console.
     * The report consists of a header, task counts, and then three sections
     * showing tasks in each state. The task sections are labeled as "ToDo Bucket",
     * "InProgress Bucket", and "Completed Bucket".
     *
     * @param completed number of completed tasks
     * @param inProgress number of tasks in progress
     * @param todo number of tasks to do
     * @param completedBucket tasks that are completed
     * @param inProgressBucket tasks that are in progress
     * @param todoBucket tasks that are to do
     */
    public void generateTextReport(long completed, long inProgress, long todo, List<ITask> completedBucket, List<ITask> inProgressBucket, List<ITask>  todoBucket) {

        // Printing total tasks section
        System.out.println("--- Report ---");
        System.out.println("Completed: " + completed);
        System.out.println("In Progress: " + inProgress);
        System.out.println("To Do: " + todo);

        //Printing buckets {completed:{},inprogress:{}, to-do:{}}
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
