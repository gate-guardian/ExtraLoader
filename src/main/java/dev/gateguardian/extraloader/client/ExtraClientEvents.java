package dev.gateguardian.extraloader.client;

import dev.gateguardian.extraloader.client.registry.ExtraClientCommands;
import dev.gateguardian.extraloader.common.ExtraLoader;
import lombok.experimental.UtilityClass;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@UtilityClass
@EventBusSubscriber(modid = ExtraLoader.MOD_ID, value = Dist.CLIENT)
public class ExtraClientEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        ExtraClientCommands.init(dispatcher);
    }
}
