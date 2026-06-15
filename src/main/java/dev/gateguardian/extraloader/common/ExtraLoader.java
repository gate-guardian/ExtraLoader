package dev.gateguardian.extraloader.common;

import dev.gateguardian.extraloader.common.pack.ExtraRepositorySource;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@Accessors(fluent = true)
public class ExtraLoader {

    public static final String MOD_ID = "extraloader";
    public static final String MOD_NAME = "Extra Loader";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    /**
     * Global pack directory path: <game_dir>/extraloader/
     */
    public static final Path GLOBAL_PACK_DIR = FMLPaths.GAMEDIR.get().resolve(MOD_ID);

    /**
     * System global pack directory path: <user_home>/.extraloader/<mc_version>/
     */
    public static final Path SYSTEM_GLOBAL_PACK_DIR = Path.of(
            System.getProperty("user.home"),
            "." + MOD_ID,
            FMLLoader.getCurrent().getVersionInfo().mcVersion()
    );

    @Getter
    @UnknownNullability
    protected static ExtraLoader instance;

    public ExtraLoader(IEventBus modEventBus) {
        if (instance != null) {
            throw new IllegalStateException("ExtraLoader instance already exists");
        }
        instance = this;

        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, ExtraConfig.commonSpec);
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, ExtraConfig.clientSpec);

        modEventBus.addListener(this::addPackFinders);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    protected void addPackFinders(AddPackFindersEvent event) {
        ExtraConfig.loadConfigEarly(ModConfig.Type.COMMON);
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(new ExtraRepositorySource(GLOBAL_PACK_DIR, PackType.SERVER_DATA));
            if (ExtraConfig.COMMON.enableSystemGlobalPacks.get()) {
                event.addRepositorySource(new ExtraRepositorySource(SYSTEM_GLOBAL_PACK_DIR, PackType.SERVER_DATA));
            }
        }
    }

}
