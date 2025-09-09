package il.ac.hit.project.main.model.report;

import il.ac.hit.project.main.model.report.external.PdfBoxReportGenerator;

import java.io.IOException;
import java.util.List;

/**
 * Adapter in the Adapter pattern.
 * <p>
 * Purpose:
 * Exposes an external PDF generator through the IReportExporter target interface
 * so clients can export reports to PDF without depending on the external API.
 * <p>
 * Roles:
 * - Target: IReportExporter
 * - Adapter: PdfReportAdapter (this class)
 * - Adaptee: PdfBoxReportGenerator (external PDF writer)
 * - Client: Code that calls IReportExporter#export
 * <p>
 * How it adapts:
 * Builds a list of human-readable lines summarizing task counts
 * ("Completed", "In Progress", "To Do") and delegates PDF creation to the
 * adaptee with a fixed title ("Task Status Report"). IO failures are caught
 * and logged to stderr; no exception is rethrown, and no success message is printed.
 */
public class PdfReportAdapter implements IReportExporter {

    /** Underlying PDF generator responsible for writing the document. */
    private final PdfBoxReportGenerator adaptee = new PdfBoxReportGenerator();

    /**
     * Adapts ReportData into a title and content lines and writes a PDF at the given path.
     *
     * @param data non-null report data with task counts
     * @param path non-null destination file path; may overwrite if the file exists
     */
    @Override
    public void export(ReportData data, String path) {
        List<String> reportContent = List.of(
                "Completed Tasks: " + data.completedTasks(),
                "In Progress Tasks: " + data.inProgressTasks(),
                "To Do Tasks: " + data.todoTasks()
        );

        try {
            adaptee.createDocument("Task Status Report", reportContent, path);
        } catch (IOException e) {
            System.err.println("Could not export PDF report: " + e.getMessage());
        }
    }
}