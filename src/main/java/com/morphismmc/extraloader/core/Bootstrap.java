package com.morphismmc.extraloader.core;

import com.morphismmc.extraloader.ExtraLoader;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(ExtraLoader.ID)
public final class Bootstrap {

    public Bootstrap() {
        DistExecutor.unsafeRunForDist(() -> Client::new, () -> Server::new);
    }

}
