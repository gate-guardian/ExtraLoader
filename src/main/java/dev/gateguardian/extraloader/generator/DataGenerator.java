package dev.gateguardian.extraloader.generator;

import dev.gateguardian.extraloader.common.ExtraLoader;
import dev.gateguardian.extraloader.generator.provider.ExtraLanguageProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExtraLoader.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerator {

    @SubscribeEvent
    public static void bootstrap(GatherDataEvent event) {
        var generator = event.getGenerator();
        PackOutput output = event.getGenerator().getPackOutput();

        generator.addProvider(event.includeClient(), new ExtraLanguageProvider(output));
    }
}
