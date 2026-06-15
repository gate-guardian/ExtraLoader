package dev.gateguardian.extraloader.common.pack;

import dev.gateguardian.extraloader.common.ExtraLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Extra pack repository source using convention over configuration
 * <p>
 * Convention:
 * - Packs in "required" folder: auto-enabled, cannot be disabled (red)
 * - Packs in "optional" folder: disabled by default, can be enabled (aqua)
 * - Packs in "default" folder or no folder: enabled by default, can be disabled (green)
 * <p>
 * Pack type is automatically detected based on content:
 * - Packs with "assets" folder are loaded as resource packs
 * - Packs with "data" folder are loaded as data packs
 *
 * @author GateGuardian
 * @since 2.0.0
 */
public class ExtraRepositorySource implements RepositorySource {

    private final Path rootDir;
    private final PackType packType;

    public ExtraRepositorySource(Path rootDir, PackType packType) {
        this.rootDir = rootDir;
        this.packType = packType;
    }

    @Override
    public void loadPacks(Consumer<Pack> onLoad) {
        if (Files.notExists(rootDir)) {
            ExtraLoader.LOGGER.debug("Repository directory {} does not exist!", rootDir);
            return;
        }

        try (Stream<Path> paths = Files.list(rootDir)) {
            paths.forEach(path -> handleRootEntry(path, onLoad));
        } catch (IOException e) {
            ExtraLoader.LOGGER.error("Failed to load packs from repository directory {}", rootDir, e);
        }
    }

    private void handleRootEntry(Path path, Consumer<Pack> onLoad) {
        if (Files.isDirectory(path)) {
            PackLoadMode mode = PackLoadMode.fromFolderName(path.getFileName().toString());
            if (mode != null) {
                discoverPacks(path, mode, onLoad);
                return;
            }
        }
        loadPackIfValid(path, PackLoadMode.DEFAULT, onLoad);
    }

    private void discoverPacks(Path folder, PackLoadMode mode, Consumer<Pack> onLoad) {
        try (Stream<Path> paths = Files.list(folder)) {
            paths.forEach(packPath -> loadPackIfValid(packPath, mode, onLoad));
        } catch (IOException e) {
            ExtraLoader.LOGGER.error("Failed to load packs from {}", folder, e);
        }
    }

    private void loadPackIfValid(Path path, PackLoadMode mode, Consumer<Pack> onLoad) {
        Pack.ResourcesSupplier supplier;
        if (isValidFilePack(path)) {
            supplier = new FilePackResourcesSupplier(path);
        } else if (isValidPathPack(path)) {
            supplier = new PathPackResourcesSupplier(path);
        } else {
            ExtraLoader.LOGGER.warn(
                    "Skipping invalid {} pack: {}",
                    packType == PackType.SERVER_DATA ? "data" : "resource",
                    path
            );
            return;
        }
        String packName = path.getFileName().toString();
        String packId = ExtraLoader.MOD_ID + "/" + mode.getFolderName() + "/" + packName;
        Pack pack = Pack.readMetaAndCreate(
                new PackLocationInfo(
                        packId,
                        Component.literal(packName),
                        mode.getPackSource(),
                        Optional.empty()
                ),
                supplier,
                packType,
                new PackSelectionConfig(mode.isRequired(), Pack.Position.TOP, false)
        );
        if (pack != null) {
            onLoad.accept(pack);
            ExtraLoader.LOGGER.debug(
                    "Loaded {} pack: {} from {} ({})",
                    packType == PackType.SERVER_DATA ? "data" : "resource",
                    packName,
                    path.getParent().getFileName(),
                    mode.getFolderName());
        }
    }

    private boolean isValidFilePack(Path path) {
        if (!Files.isRegularFile(path)) return false;
        // Check for .zip or .jar extension
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".zip") && !fileName.endsWith(".jar")) return false;
        try (FileSystem fs = FileSystems.newFileSystem(path)) {
            // Check for pack.mcmeta
            if (!Files.isRegularFile(fs.getPath("pack.mcmeta"))) return false;
            // Auto-detect pack type based on content
            return Files.isDirectory(fs.getPath(packType.getDirectory()));
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isValidPathPack(Path path) {
        if (!Files.isDirectory(path)) return false;
        // Check for pack.mcmeta
        if (!Files.isRegularFile(path.resolve("pack.mcmeta"))) return false;
        // Auto-detect pack type based on content
        return Files.isDirectory(path.resolve(packType.getDirectory()));
    }

    /**
     * ResourcesSupplier for file-based (ZIP/JAR) packs.
     * Replaces the removed FilePackResources.FileResourcesSupplier.
     */
    private record FilePackResourcesSupplier(Path path) implements Pack.ResourcesSupplier {
        @Override
        public PackResources openPrimary(PackLocationInfo info) {
            return new FilePackResources(info, new FilePackResources.SharedZipFileAccess(path.toFile()), path.getFileName().toString());
        }

        @Override
        public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
            return openPrimary(info);
        }
    }

    /**
     * ResourcesSupplier for directory-based packs.
     * Replaces the removed PathPackResources.PathResourcesSupplier.
     */
    private record PathPackResourcesSupplier(Path path) implements Pack.ResourcesSupplier {
        @Override
        public PackResources openPrimary(PackLocationInfo info) {
            return new PathPackResources(info, path);
        }

        @Override
        public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
            return openPrimary(info);
        }
    }
}
