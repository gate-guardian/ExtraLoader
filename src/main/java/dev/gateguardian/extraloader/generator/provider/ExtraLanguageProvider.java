package dev.gateguardian.extraloader.generator.provider;

import dev.gateguardian.extraloader.common.ExtraLoader;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ExtraLanguageProvider extends LanguageProvider {

    public ExtraLanguageProvider(PackOutput output) {
        super(output, ExtraLoader.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("command.extraloader.open.success", "Opened directory: %s");
        add("command.extraloader.open.failure.notDirectory", "Path is not a directory: %s");
        add("command.extraloader.open.failure.failed", "Failed to open directory: %s");
        add("config.extraloader.enableSystemGlobalPacks", "Enable system-global packs");
        add("config.extraloader.disabledResourcePacks", "Disabled resource packs");
    }
}
