package model.report;

import model.report.external.JSONReportGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
 * - Adaptee: JSONReportGenerator (external JSON writer)
 * - Client: Code that calls IReportExporter#export
 * <p>
 * How it adapts:
 * Delegates to the adaptee to serialize the provided ReportData to JSON and write it to the given path.
 * Success is reported to stdout; IO failures are caught and logged to stderr.
 */
public class JsonReportAdapter implements IReportExporter {
    /** Underlying JSON generator responsible for writing the file. */
    private final JSONReportGenerator generator = new JSONReportGenerator();

    /**
     * Adapts ReportData to the JSON generator's API and writes a file at the given path.
     *
     * @param data non-null report data to serialize
     * @param path non-null destination file path; may overwrite if the file exists
     */
    @Override
    public void export(ReportData data, String path) {
        try {
            //Convert ITask to a record class to avoid infinite JSON loop.
            record TasksDTO(int ID, String title, String description, String state, String priority, String creationDate) {}
            record TotalTasksDTO(String type, int totalTasks) {}
            record FinalData(List<TotalTasksDTO> totalTasksDTO, List<TasksDTO> tasksDTO ) {}

            //Two sections to JSON document -1: total tasks per category
            List<TotalTasksDTO> totalTasks = new ArrayList<>();
            totalTasks.add(new TotalTasksDTO("Completed",(int)data.completedTasks()));
            totalTasks.add(new TotalTasksDTO("InProgress",(int)data.inProgressTasks()));
            totalTasks.add(new TotalTasksDTO("To Do", (int)data.todoTasks()));

            //2: Bucket of each task category
            List<TasksDTO> tasks =
                Stream.of(
                data.completedTasksBucket().stream(),
                data.inProgressTasksBucket().stream(),
                data.toDoTasksBucket().stream()
                        ).flatMap(stream -> stream.map(t -> new TasksDTO(
                            t.getId(),
                            t.getTitle(),
                            t.getDescription(),
                            t.getState().getDisplayName(),
                            t.getPriority().getDisplayName(),
                            t.getCreationDate().toLocaleString()
                        )))
                        .toList();
            //3: Joint JSON document.
            FinalData sendData = new FinalData(totalTasks, tasks);
            generator.createJson(sendData, path);
            System.out.println("JSON document generated successfully at: " + path);
        } catch (IOException e) {
            System.err.println("Error generating JSON document: " + e.getMessage());
        }
    }
}