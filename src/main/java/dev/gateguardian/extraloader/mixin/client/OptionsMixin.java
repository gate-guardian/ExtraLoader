package dev.gateguardian.extraloader.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.gateguardian.extraloader.common.ExtraConfig;
import dev.gateguardian.extraloader.common.ExtraLoader;
import dev.gateguardian.extraloader.common.pack.PackLoadMode;
import net.minecraft.client.Options;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @WrapOperation(
            method = "updateResourcePacks(Lnet/minecraft/server/packs/repository/PackRepository;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;equals(Ljava/lang/Object;)Z")
    )
    private boolean updateDisabledResourcePacks(List<String> instance, Object o, Operation<Boolean> original) {
        //noinspection unchecked
        List<String> oldPacks = (List<String>) o;
        List<String> oldList = new ArrayList<>(ExtraConfig.CLIENT.disabledResourcePacks.get());
        oldList.addAll(oldPacks);
        oldList.removeAll(instance);
        ExtraConfig.CLIENT.disabledResourcePacks.set(oldList);
        return original.call(instance, o);
    }

    @WrapOperation(
            method = "loadSelectedResourcePacks(Lnet/minecraft/server/packs/repository/PackRepository;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;setSelected(Ljava/util/Collection;)V")
    )
    private void enableDefaultResourcePacks(PackRepository instance, Collection<String> ids, Operation<Void> original) {
        instance.getAvailableIds().stream()
                .filter(id -> id.startsWith(ExtraLoader.MOD_ID + "/" + PackLoadMode.DEFAULT.getFolderName()))
                .forEach(ids::add);
        ids.removeIf(id -> ExtraConfig.CLIENT.disabledResourcePacks.get().contains(id));
        original.call(instance, ids);
    }
}
