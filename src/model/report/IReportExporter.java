package model.report;

/**
 * Contract for exporting report data (e.g., CSV, JSON, PDF).
 * Implementations define the target format and error handling.
 */
public interface IReportExporter {

    /**
     * Exports the given data to the destination path.
     *
     * @param data report data; must not be null
     * @param path destination path; must not be null or empty
     */
    void export(ReportData data, String path);
}