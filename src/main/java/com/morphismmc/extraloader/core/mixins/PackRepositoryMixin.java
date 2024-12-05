package com.morphismmc.extraloader.core.mixins;

import com.morphismmc.extraloader.ExtraLoader;
import com.morphismmc.extraloader.core.Config;
import com.morphismmc.extraloader.pack.LoaderConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.nio.file.Path;
import java.util.*;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {

    @Shadow private Map<String, Pack> available;

    @ModifyArg(
            method = "setSelected(Ljava/util/Collection;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/repository/PackRepository;rebuildSelected(Ljava/util/Collection;)Ljava/util/List;")
    )
    private Collection<String> modifySelected(Collection<String> ids) {
        if (ids.isEmpty()) return ids;
        // check that Loader Config has been applied
        if (ids.stream().anyMatch(id -> id.startsWith(ExtraLoader.ID))) return ids;

        var path = ids.contains("mod_resources")
                ? Config.CLIENT.resourcepacksPath.get()
                : Config.COMMON.datapacksPath.get();
        var config = LoaderConfig.load(Path.of(path));
        var list = new ArrayList<>(ids);
        if (config.blackList()) {
            available.keySet().stream()
                    .filter(id -> id.startsWith(ExtraLoader.ID))
                    .filter(id -> !config.packs().contains(id))
                    .forEach(list::add);
        } else {
            list.addAll(config.packs());
        }
        return list;
    }
}
