package com.morphismmc.extraloader.pack;

import com.morphismmc.extraloader.ExtraLoader;
import com.morphismmc.extraloader.utils.FileUtils;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.morphismmc.extraloader.utils.FileUtils.GSON;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record LoaderConfig(boolean blackList, List<String> packs) {

    private static final LoaderConfig DEFAULT = new LoaderConfig(true, Collections.emptyList());

    private static final String NAME = "config.json";

    public LoaderConfig {
        Collections.reverse(packs);
        packs.replaceAll(s -> ExtraLoader.ID + "/" + s);
    }

    public static LoaderConfig load(Path path) {
        var filePath = path.resolve(NAME);
        var file = filePath.toFile();
        if (!file.exists() && !FileUtils.saveJson(path, DEFAULT)) {
            ExtraLoader.LOGGER.warn("Could not create config file!");
        } else {
            var element = FileUtils.loadJson(filePath);
            if (element != null) {
                return GSON.fromJson(element, LoaderConfig.class);
            }
        }
        return DEFAULT;
    }
}
