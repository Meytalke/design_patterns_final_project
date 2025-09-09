package il.ac.hit.project.main.model.report;

import il.ac.hit.project.main.model.report.external.JsonReportGenerator;

import java.io.IOException;

/**
 * Adapter in the Adapter pattern.
 * <p>
 * Purpose:
 * Exposes an external JSON generator through the IReportExporter target interface
 * so clients can export reports without depending on the external API.
 * <p>
 * Roles:
 * - Target: IReportExporter
 * - Adapter: JsonReportAdapter (this class)
 * - Adaptee: JsonReportGenerator (external JSON writer)
 * - Client: Code that calls IReportExporter#export
 * <p>
 * How it adapts:
 * Delegates to the adaptee to serialize the provided ReportData to JSON and write it to the given path.
 * Success is reported to stdout; IO failures are caught and logged to stderr.
 */
public class JsonReportAdapter implements IReportExporter {
    /** Underlying JSON generator responsible for writing the file. */
    private final JsonReportGenerator generator = new JsonReportGenerator();

    /**
     * Adapts ReportData to the JSON generator's API and writes a file at the given path.
     *
     * @param data non-null report data to serialize
     * @param path non-null destination file path; may overwrite if the file exists
     */
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