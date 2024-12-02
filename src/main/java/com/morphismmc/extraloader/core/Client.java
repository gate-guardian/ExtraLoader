package com.morphismmc.extraloader.core;

import com.morphismmc.extraloader.pack.ExtraRepositorySource;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;

import java.nio.file.Path;

final class Client extends Common {

    @Override
    protected void addPackFinders(AddPackFindersEvent event) {
        super.addPackFinders(event);
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource(new ExtraRepositorySource(
                    Path.of(Config.CLIENT.resourcepacksPath.get()),
                    PackType.CLIENT_RESOURCES, PackSource.DEFAULT, true));
        }
    }
}
