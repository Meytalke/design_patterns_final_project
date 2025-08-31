package model.report;

public interface IReportExporter {
    void export(ReportData data, String path);
}
