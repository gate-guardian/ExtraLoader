package com.morphismmc.extraloader.pack;

import com.morphismmc.extraloader.utils.FileUtils;

import java.nio.file.Path;

import static com.morphismmc.extraloader.utils.FileUtils.GSON;

public record LoaderConfig(boolean blackList, String... packs) {

    private static final LoaderConfig DEFAULT = new LoaderConfig(true);

    private static final String NAME = "config.json";

    public static LoaderConfig load(Path path) {
        var filePath = path.resolve(NAME);
        var file = filePath.toFile();
        LoaderConfig config = DEFAULT;
        if (!file.exists()) {
            FileUtils.saveJson(filePath, config);
        } else {
            var element = FileUtils.loadJson(filePath);
            if (element != null) {
                config = GSON.fromJson(element, LoaderConfig.class);
            }
        }
        return config;
    }
}
