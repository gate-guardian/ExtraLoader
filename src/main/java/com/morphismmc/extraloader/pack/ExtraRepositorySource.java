package com.morphismmc.extraloader.pack;

import com.morphismmc.extraloader.ExtraLoader;
import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class ExtraRepositorySource implements RepositorySource {

    private final Path folder;
    private final PackType packType;
    private final PackSource packSource;
    private final boolean require;

    public ExtraRepositorySource(Path folder, PackType packType, PackSource packSource, boolean require) {
        this.folder = folder;
        this.packType = packType;
        this.packSource = packSource;
        this.require = require;
    }

    private static String nameFromPath(Path path) {
        return path.getFileName().toString();
    }

    public void loadPacks(Consumer<Pack> onLoad) {
        try {
            FileUtil.createDirectoriesSafe(this.folder);
            FolderRepositorySource.discoverPacks(folder, false, (path, supplier) -> {
                String name = nameFromPath(path);
                Pack pack = Pack.readMetaAndCreate("file/" + name, Component.literal(name),
                        require, supplier, packType, Pack.Position.TOP, packSource);
                if (pack != null) {
                    onLoad.accept(pack);
                }
            });
        } catch (IOException ex) {
            ExtraLoader.LOGGER.warn("Failed to list packs in {}", folder, ex);
        }
    }
}
