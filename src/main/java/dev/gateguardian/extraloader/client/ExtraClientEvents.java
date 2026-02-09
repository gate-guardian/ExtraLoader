package dev.gateguardian.extraloader.client;

import dev.gateguardian.extraloader.client.registry.ExtraClientCommands;
import dev.gateguardian.extraloader.common.ExtraLoader;
import lombok.experimental.UtilityClass;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@UtilityClass
@Mod.EventBusSubscriber(modid = ExtraLoader.MOD_ID, value = Dist.CLIENT)
public class ExtraClientEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        ExtraClientCommands.init(dispatcher);
    }
}
