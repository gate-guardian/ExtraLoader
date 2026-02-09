package dev.gateguardian.extraloader.client;

import dev.gateguardian.extraloader.common.ExtraConfig;
import dev.gateguardian.extraloader.common.ExtraLoader;
import dev.gateguardian.extraloader.common.pack.ExtraRepositorySource;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ExtraLoaderClient extends ExtraLoader {

    public ExtraLoaderClient(FMLJavaModLoadingContext context) {
        super(context);
    }

    @Override
    protected void addPackFinders(AddPackFindersEvent event) {
        super.addPackFinders(event);
        ExtraConfig.loadConfigEarly(ModConfig.Type.CLIENT);
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource(new ExtraRepositorySource(GLOBAL_PACK_DIR, PackType.CLIENT_RESOURCES));
            if (ExtraConfig.COMMON.enableSystemGlobalPacks.get()) {
                event.addRepositorySource(new ExtraRepositorySource(SYSTEM_GLOBAL_PACK_DIR, PackType.CLIENT_RESOURCES));
            }
        }
    }

}
