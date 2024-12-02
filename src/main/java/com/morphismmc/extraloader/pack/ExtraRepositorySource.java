package com.morphismmc.extraloader.pack;

import com.morphismmc.extraloader.ExtraLoader;
import net.minecraft.ChatFormatting;
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

    private static final PackSource EXTRA = PackSource.create(name -> Component
                    .translatable("pack.nameAndSource", name, ExtraLoader.NAME)
                    .withStyle(ChatFormatting.AQUA), true);

    private final Path folder;
    private final PackType packType;

    public ExtraRepositorySource(Path folder, PackType packType) {
        this.folder = folder;
        this.packType = packType;
    }

    private static String nameFromPath(Path path) {
        return path.getFileName().toString();
    }

    public void loadPacks(Consumer<Pack> onLoad) {
        try {
            FileUtil.createDirectoriesSafe(this.folder);
            FolderRepositorySource.discoverPacks(folder, false, (path, supplier) -> {
                String name = nameFromPath(path);
                Pack pack = Pack.readMetaAndCreate(ExtraLoader.ID + "/" + name, Component.literal(name),
                        false, supplier, packType, Pack.Position.TOP, EXTRA);
                if (pack != null) {
                    onLoad.accept(pack);
                }
            });
        } catch (IOException ex) {
            ExtraLoader.LOGGER.warn("Failed to list packs in {}", folder, ex);
        }
    }
}
