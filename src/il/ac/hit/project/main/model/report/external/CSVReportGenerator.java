package il.ac.hit.project.main.model.report.external;

import il.ac.hit.project.main.model.task.ITask;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * CSVReportGenerator
 * Responsible for generating CSV files representing report data.
 * This class provides functionality to create a CSV file from a given set of data.
 */
public class CSVReportGenerator {

    /**
     * Creates a summary CSV report from a given map of report data.
     * The CSV file's columns are Type and Count.
     *
     * @param reportData non-null map with report data; keys are task types (e.g., "Completed", "In Progress", "To Do")
     *                   and values are the corresponding task counts
     * @param path non-null destination file path; may overwrite if the file exists
     * @throws IOException if an IO error occurs writing to the given path
     */
    public void createSummaryCSV(Map<String, Long> reportData, String path) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("Type", "Count"))) {

            for (Map.Entry<String, Long> entry : reportData.entrySet()) {
                csvPrinter.printRecord(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Creates a detailed CSV report from a given list of tasks.
     * The CSV file's columns are ID, Title, Description and State.
     *
     * @param tasks non-null list of tasks to include in the report
     * @param path non-null destination file path; may overwrite if the file exists
     * @throws IOException if an IO error occurs writing to the given path
     */
    public void createDetailedCSV(List<ITask> tasks, String path) throws IOException {

        try (FileWriter fileWriter = new FileWriter(path);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter,
                     CSVFormat.DEFAULT.withHeader("ID", "Title", "Description", "State"))){

            for(ITask task : tasks){csvPrinter.printRecord(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getState().getDisplayName()
            );}
        }
    }
}
