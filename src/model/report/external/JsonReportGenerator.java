package model.report.external;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;

public class JsonReportGenerator {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void createJson(Object data, String path) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(data, writer);
        }
    }
}
