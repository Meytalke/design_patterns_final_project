package model.report;

import model.report.external.CSVReportGenerator;
import model.task.ITask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
 * - Adaptee: CSVReportGenerator (external CSV writer)
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
    private final CSVReportGenerator generator = new CSVReportGenerator();

    /**
     * Adapts ReportData to the CSV generator's expected input and writes a file at the given path.
     * The resulting CSV contains two columns: Type and Count, with rows ordered as
     * "Completed", "In Progress", and "To Do".
     * 
     * The detailed CSV file contains columns for ID, Title, Description, State, Priority, and CreationDate.
     *
     * @param data non-null report data with task counts
     * @param path non-null destination file path; may overwrite if the file exists
     */
    @Override
    public void export(ReportData data, String path) {
        // preserve row order in CSV
        Map<String, Long> summaryReportData = new LinkedHashMap<>(); 
        summaryReportData.put("Completed", data.completedTasks());
        summaryReportData.put("In Progress", data.inProgressTasks());
        summaryReportData.put("To Do", data.todoTasks());
        // combine completed, in-progress, and to-do tasks
        List<ITask> detailedReportData = new ArrayList<>(data.completedTasksBucket());
        detailedReportData.addAll(data.inProgressTasksBucket());
        detailedReportData.addAll(data.toDoTasksBucket());

        //generate reports
        try {
            generator.createSummaryCSV(summaryReportData, "summary-"+ path);
            generator.createDetailedCSV(detailedReportData, "detailed-"+path);
            System.out.println("CSV document generated successfully at: " + path);
        } catch (IOException e) {
            System.err.println("Error generating CSV document: " + e.getMessage());
        }
    }
}