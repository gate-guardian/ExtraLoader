package com.morphismmc.extraloader.core;

import com.morphismmc.extraloader.ExtraLoader;
import com.morphismmc.extraloader.pack.ExtraRepositorySource;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Path;

public abstract sealed class Common implements ExtraLoader permits Client, Server {

    public static ExtraLoader instance;

    public Common() {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = this;

        Config.register(ModLoadingContext.get());

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::addPackFinders);
    }

    protected void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(new ExtraRepositorySource(
                    Path.of(Config.COMMON.datapacksPath.get()), PackType.SERVER_DATA));
        }
    }

}
