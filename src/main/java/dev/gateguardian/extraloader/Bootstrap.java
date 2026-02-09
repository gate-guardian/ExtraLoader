package dev.gateguardian.extraloader;

import dev.gateguardian.extraloader.client.ExtraLoaderClient;
import dev.gateguardian.extraloader.common.ExtraLoader;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExtraLoader.MOD_ID)
public class Bootstrap {

    public Bootstrap(FMLJavaModLoadingContext context) {
        DistExecutor.unsafeRunForDist(
                () -> () -> new ExtraLoaderClient(context),
                () -> () -> new ExtraLoader(context)
        );
    }
}
