package com.morphismmc.extraloader.core;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.base.Joiner;
import com.morphismmc.extraloader.ExtraLoader;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = ExtraLoader.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {

    private static final Joiner PATH_JOINER = Joiner.on(File.separator);

    private static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        var pair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = pair.getRight();
        CLIENT = pair.getLeft();
        loadConfigEarly(CLIENT_SPEC, ModConfig.Type.CLIENT);
    }

    private static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        var pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = pair.getRight();
        COMMON = pair.getLeft();
    }

    public static final class Client {

        public final ForgeConfigSpec.ConfigValue<String> resourcepacksPath;

        private Client(ForgeConfigSpec.Builder builder) {
            resourcepacksPath = builder
                    .comment("resourcepacks Path")
                    .define("resourcepacksPath", getDefaultPath("resourcepacks"));
        }
    }

    public static final class Common {

        public final ForgeConfigSpec.ConfigValue<String> datapacksPath;

        private Common(ForgeConfigSpec.Builder builder) {
            datapacksPath = builder
                    .comment("datapacksPath")
                    .define("datapacks Path", getDefaultPath("datapacks"));
        }
    }

    static void register(ModLoadingContext ctx) {
        ctx.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        ctx.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            checkPath(COMMON.datapacksPath.get());
        }

        if (event.getConfig().getSpec() == CLIENT_SPEC) {
            checkPath(CLIENT.resourcepacksPath.get());
        }
    }

    private static void checkPath(String path) {
        try {
            Files.createDirectories(Path.of(path));
        } catch (IOException ignored) {
        }
    }

    private static String getDefaultPath(String path) {
        return PATH_JOINER.join(System.getProperty("user.home"), "." + ExtraLoader.ID, path);
    }

    private static void loadConfigEarly(ForgeConfigSpec spec, ModConfig.Type type) {
        var file = ExtraLoader.ID + "-" + type.extension() + ".toml";
        var configData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve(file))
                .preserveInsertionOrder()
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
        configData.load();
        spec.setConfig(configData);
    }
}
