package model.report;

import model.report.external.PdfBoxReportGenerator;

import java.io.IOException;
import java.util.List;

public class PdfReportAdapter implements IReportExporter {

    private final PdfBoxReportGenerator adaptee = new PdfBoxReportGenerator();

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
