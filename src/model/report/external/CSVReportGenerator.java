package model.report.external;

import model.task.ITask;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CSVReportGenerator
 * Responsible for generating CSV files representing report data.
 * This class provides functionality to create a CSV file from a given set of data.
 */
public class CSVReportGenerator {

    public void createSummaryCSV(Map<String, Long> reportData, String path) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("Type", "Count"))) {

            for (Map.Entry<String, Long> entry : reportData.entrySet()) {
                csvPrinter.printRecord(entry.getKey(), entry.getValue());
            }
        }
    }

    public void createDetailedCSV(List<ITask> tasks, String path) throws IOException {

        try (FileWriter fileWriter = new FileWriter(path);
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter,
                        CSVFormat.DEFAULT.withHeader("ID", "Title", "Description", "State", "Priority", "CreationDate"))){

            for(ITask task : tasks){csvPrinter.printRecord(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getState().getDisplayName(),
                    task.getPriority().getDisplayName(),
                    task.getCreationDate().toLocaleString()
            );}
        }
    }
}
