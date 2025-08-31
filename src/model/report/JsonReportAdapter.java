package model.report;

import model.report.external.JsonReportGenerator;

import java.io.IOException;

public class JsonReportAdapter implements IReportExporter {
    private final JsonReportGenerator generator = new JsonReportGenerator();

    @Override
    public void export(ReportData data, String path) {
        try {
            generator.createJson(data, path);
            System.out.println("JSON document generated successfully at: " + path);
        } catch (IOException e) {
            System.err.println("Error generating JSON document: " + e.getMessage());
        }
    }
}
