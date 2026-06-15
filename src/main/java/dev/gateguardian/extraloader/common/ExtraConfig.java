package dev.gateguardian.extraloader.common;

import lombok.experimental.UtilityClass;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class ExtraConfig {

    public final Common COMMON;
    public final Client CLIENT;
    final ModConfigSpec commonSpec;
    final ModConfigSpec clientSpec;

    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    static {
        final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    /**
     * Load configuration early (before Forge configuration initialization)
     * <p>
     * In NeoForge 1.21.1, configurations are automatically loaded by the framework
     * after being registered via {@code ModContainer.registerConfig()}.
     * This method is kept for forward compatibility and early-access scenarios.
     */
    public static void loadConfigEarly(ModConfig.Type type) {
        // Configs are auto-loaded by the framework in 1.21.1+
        // The config values (ConfigValue<?>) will have their data available
        // once the ModConfigEvent.Loading event has completed.
    }

    public final class Common {

        public final ModConfigSpec.BooleanValue enableSystemGlobalPacks;

        public Common(ModConfigSpec.Builder enableSystemGlobalPacks) {
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

        public final ModConfigSpec.ConfigValue<List<? extends String>> disabledResourcePacks;

        public Client(ModConfigSpec.Builder builder) {
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
