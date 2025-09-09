package il.ac.hit.project.main.model.report.external;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Responsible for generating JSON files from given data.
 * This class uses the Gson library to convert objects to their JSON representations.
 */
public class JsonReportGenerator {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void createJson(Object data, String path) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(data, writer);
        }
    }
}
