package model.report;

import model.report.external.CsvReportGenerator;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsvReportAdapter implements IReportExporter {
    private final CsvReportGenerator generator = new CsvReportGenerator();

    @Override
    public void export(ReportData data, String path) {
        Map<String, Long> reportData = new LinkedHashMap<>();
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
