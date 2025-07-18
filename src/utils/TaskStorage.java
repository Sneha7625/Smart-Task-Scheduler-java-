// TaskStorage.java
package utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.Task;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class TaskStorage {
    private static final String FILE_NAME = "tasks.json";

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, type, context) ->
                    new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) ->
                    LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, type, context) ->
                    new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, type, context) ->
                    LocalTime.parse(json.getAsString()))
            .create();

    public static void saveTasks(List<Task> tasks) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(FILE_NAME))) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Task> loadTasks() {
        if (!Files.exists(Paths.get(FILE_NAME))) return new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Paths.get(FILE_NAME))) {
            Type taskListType = new TypeToken<ArrayList<Task>>() {}.getType();
            return gson.fromJson(reader, taskListType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}