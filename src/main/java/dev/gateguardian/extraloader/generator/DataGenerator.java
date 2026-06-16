package dev.gateguardian.extraloader.generator;

import dev.gateguardian.extraloader.common.ExtraLoader;
import dev.gateguardian.extraloader.generator.provider.ExtraLanguageProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ExtraLoader.MOD_ID)
public class DataGenerator {

    @SubscribeEvent
    public static void bootstrap(GatherDataEvent.Client event) {
        event.getGenerator().addProvider(true, new ExtraLanguageProvider(event.getGenerator().getPackOutput()));
    }
}
