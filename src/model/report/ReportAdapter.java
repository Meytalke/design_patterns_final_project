package model.report;

import model.report.external.SimpleTextReporter;

public class ReportAdapter implements IReportExporter {
    private final SimpleTextReporter adaptee = new SimpleTextReporter();

    @Override
    public void export(ReportData data, String path) {
        // Adapt the ReportData record to the external library's method signature
        adaptee.generateTextReport(data.completedTasks(), data.inProgressTasks(), data.todoTasks());
    }
}
