package com.morphismmc.extraloader.core;

import com.morphismmc.extraloader.pack.ExtraRepositorySource;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AddPackFindersEvent;

import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
final class Client extends Common {

    @Override
    protected void addPackFinders(AddPackFindersEvent event) {
        super.addPackFinders(event);
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource(new ExtraRepositorySource(
                    Path.of(Config.CLIENT.resourcepacksPath.get()), PackType.CLIENT_RESOURCES));
        }
    }

}
