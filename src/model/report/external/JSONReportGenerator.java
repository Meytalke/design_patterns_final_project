package model.report.external;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Responsible for generating JSON files from given data.
 * This class uses the Gson library to convert objects to their JSON representations.
 */
public class JSONReportGenerator {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Serializes the given data to its JSON representation and writes it to the given path.
     *
     * @param data non-null object to serialize
     * @param path non-null destination file path; may overwrite if the file exists
     * @throws IOException if the file cannot be written
     */
    public void createJson(Object data, String path) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(data, writer);
        }
    }
}
