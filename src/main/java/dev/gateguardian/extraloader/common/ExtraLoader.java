package dev.gateguardian.extraloader.common;

import dev.gateguardian.extraloader.common.pack.ExtraRepositorySource;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
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
     * Global pack directory path: <user_home>/.extraloader/<mc_version>/
     */
    public static final Path SYSTEM_GLOBAL_PACK_DIR = Path.of(
            System.getProperty("user.home"),
            "." + MOD_ID,
            FMLLoader.versionInfo().mcVersion()
    );

    @Getter
    protected static ExtraLoader instance;

    public ExtraLoader(FMLJavaModLoadingContext context) {
        if (instance != null) {
            throw new IllegalStateException("ExtraLoader instance already exists");
        }
        instance = this;

        context.registerConfig(ModConfig.Type.COMMON, ExtraConfig.commonSpec);
        context.registerConfig(ModConfig.Type.CLIENT, ExtraConfig.clientSpec);

        var modBus = context.getModEventBus();
        modBus.addListener(this::addPackFinders);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
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
