package model.report;

import model.report.external.SimpleTextReporter;

/**
 * Adapter in the Adapter pattern.
 * <p>
 * Purpose:
 * Exposes an external console reporter through the IReportExporter target interface,
 * so clients can generate reports without depending on the external API.
 * <p>
 * Roles:
 * - Target: IReportExporter
 * - Adapter: ReportAdapter (this class)
 * - Adaptee: SimpleTextReporter (external reporter)
 * - Client: Code that calls IReportExporter#export
 * <p>
 * How it adapts:
 * Unpacks counts from ReportData and delegates to the adaptee's method that expects
 * three numeric arguments. The path parameter is ignored because the adaptee writes
 * to the console instead of a file.
 */
public class ReportAdapter implements IReportExporter {
    private final SimpleTextReporter adaptee = new SimpleTextReporter();

    /**
     * Adapts ReportData to the console reporter's method signature.
     *
     * @param data non-null report data with task counts
     * @param path ignored (no file is produced by the console reporter)
     */
    @Override
    public void export(ReportData data, String path) {
        // Adapt the ReportData record to the external library's method signature
        adaptee.generateTextReport(data.completedTasks(), data.inProgressTasks(), data.todoTasks());
    }
}