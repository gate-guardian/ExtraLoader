package dev.gateguardian.extraloader;

import dev.gateguardian.extraloader.client.ExtraLoaderClient;
import dev.gateguardian.extraloader.common.ExtraLoader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(ExtraLoader.MOD_ID)
public class Bootstrap {

    public Bootstrap(IEventBus modEventBus) {
        if (FMLEnvironment.dist.isClient()) {
            new ExtraLoaderClient(modEventBus);
        } else {
            new ExtraLoader(modEventBus);
        }
    }
}
