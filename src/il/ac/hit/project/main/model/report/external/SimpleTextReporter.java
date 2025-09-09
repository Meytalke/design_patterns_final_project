package model.report.external;

/**
 * A simple text-based reporter for generating console output reports.
 * This class is responsible for printing a formatted report to the console
 * that summarizes the counts of completed tasks, tasks in progress, and tasks to do.
 */
public class SimpleTextReporter {
    public void generateTextReport(long completed, long inProgress, long todo) {
        System.out.println("--- Report ---");
        System.out.println("Completed: " + completed);
        System.out.println("In Progress: " + inProgress);
        System.out.println("To Do: " + todo);
        System.out.println("--- End of Report ---");
    }
}
