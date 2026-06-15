package dev.gateguardian.extraloader.generator;

import dev.gateguardian.extraloader.common.ExtraLoader;
import dev.gateguardian.extraloader.generator.provider.ExtraLanguageProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ExtraLoader.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerator {

    @SubscribeEvent
    public static void bootstrap(GatherDataEvent event) {
        var generator = event.getGenerator();
        PackOutput output = event.getGenerator().getPackOutput();

        generator.addProvider(event.includeClient(), new ExtraLanguageProvider(output));
    }
}
