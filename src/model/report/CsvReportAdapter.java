package model.report;

import model.report.external.CsvReportGenerator;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adapter in the Adapter pattern.
 * <p>
 * Purpose:
 * Exposes an external CSV generator through the IReportExporter target interface
 * so clients can export reports without depending on the external API.
 * <p>
 * Roles:
 * - Target: IReportExporter
 * - Adapter: CsvReportAdapter (this class)
 * - Adaptee: CsvReportGenerator (external CSV writer)
 * - Client: Code that calls IReportExporter#export
 * <p>
 * How it adapts:
 * Transforms ReportData into an ordered map with rows for "Completed",
 * "In Progress", and "To Do" and delegates CSV creation to the adaptee.
 * Row order is preserved using LinkedHashMap. Success is reported to stdout;
 * IO failures are caught and logged to stderr (no exception is rethrown).
 */
public class CsvReportAdapter implements IReportExporter {

    /** Underlying CSV generator responsible for writing the file. */
    private final CsvReportGenerator generator = new CsvReportGenerator();

    /**
     * Adapts ReportData to the CSV generator's expected input and writes a file at the given path.
     * The resulting CSV contains two columns: Type and Count, with rows ordered as
     * "Completed", "In Progress", and "To Do".
     *
     * @param data non-null report data with task counts
     * @param path non-null destination file path; may overwrite if the file exists
     */
    @Override
    public void export(ReportData data, String path) {
        Map<String, Long> reportData = new LinkedHashMap<>(); // preserve row order in CSV
        reportData.put("Completed", data.completedTasks());
        reportData.put("In Progress", data.inProgressTasks());
        reportData.put("To Do", data.todoTasks());

        try {
            generator.createCsv(reportData, path);
            System.out.println("CSV document generated successfully at: " + path);
        } catch (IOException e) {
            System.err.println("Error generating CSV document: " + e.getMessage());
        }
    }
}