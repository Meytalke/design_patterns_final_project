package model.report.external;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Responsible for generating CSV files representing report data.
 * This class provides functionality to create a CSV file from a given set of data.
 */
public class CsvReportGenerator {

    public void createCsv(Map<String, Long> reportData, String path) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("Type", "Count"))) {

            for (Map.Entry<String, Long> entry : reportData.entrySet()) {
                csvPrinter.printRecord(entry.getKey(), entry.getValue());
            }
        }
    }
}
