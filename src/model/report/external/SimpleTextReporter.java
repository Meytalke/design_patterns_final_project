package model.report.external;

public class SimpleTextReporter {
    public void generateTextReport(long completed, long inProgress, long todo) {
        System.out.println("--- Report ---");
        System.out.println("Completed: " + completed);
        System.out.println("In Progress: " + inProgress);
        System.out.println("To Do: " + todo);
        System.out.println("--- End of Report ---");
    }
}
