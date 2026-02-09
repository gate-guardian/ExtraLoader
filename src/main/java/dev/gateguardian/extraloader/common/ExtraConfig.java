package dev.gateguardian.extraloader.common;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import lombok.experimental.UtilityClass;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class ExtraConfig {

    public final Common COMMON;
    public final Client CLIENT;
    final ForgeConfigSpec commonSpec;
    final ForgeConfigSpec clientSpec;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    /**
     * Load configuration early (before Forge configuration initialization)
     */
    public static void loadConfigEarly(ModConfig.Type type) {
        var spec = type == ModConfig.Type.COMMON ? commonSpec : clientSpec;
        var file = ExtraLoader.MOD_ID + "-" + type.extension() + ".toml";
        var configData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve(file))
                .preserveInsertionOrder()
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
        configData.load();
        spec.setConfig(configData);
    }

    public final class Common {

        public final ForgeConfigSpec.BooleanValue enableSystemGlobalPacks;

        public Common(ForgeConfigSpec.Builder enableSystemGlobalPacks) {
            enableSystemGlobalPacks
                    .comment("Extra Loader Configuration")
                    .comment("Convention over configuration:")
                    .comment("  - Place packs in 'required' folder: auto-enabled, cannot be disabled (red)")
                    .comment("  - Place packs in 'optional' folder: disabled by default, can be enabled (aqua)")
                    .comment("  - Place packs in 'default' folder or no folder: enabled by default, can be disabled (green)")
                    .comment("Pack type is automatically detected:")
                    .comment("  - Packs with 'assets' folder are loaded as resource packs")
                    .comment("  - Packs with 'data' folder are loaded as data packs");

            this.enableSystemGlobalPacks = enableSystemGlobalPacks
                    .translation("config.extraloader.enableSystemGlobalPacks")
                    .comment("Whether system-global packs are enabled.")
                    .comment("If disabled, packs located in the system-global directory will be ignored.")
                    .define("enableSystemGlobalPacks", true);
        }
    }

    public final class Client {

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> disabledResourcePacks;

        public Client(ForgeConfigSpec.Builder builder) {
            disabledResourcePacks = builder
                    .translation("config.extraloader.disabledResourcePacks")
                    .comment("List of disabled resource packs.")
                    .comment("Entries should match the pack id.")
                    .defineListAllowEmpty(
                            "disabledResourcePacks",
                            Collections::emptyList,
                            o -> o instanceof String s && !s.isBlank()
                    );
        }
    }
}
