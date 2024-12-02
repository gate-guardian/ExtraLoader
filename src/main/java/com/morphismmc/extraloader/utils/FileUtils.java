package com.morphismmc.extraloader.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static JsonElement loadJson(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader);
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean saveJson(Path path, Object json) {
        try {
            Files.writeString(path, GSON.toJson(json));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
