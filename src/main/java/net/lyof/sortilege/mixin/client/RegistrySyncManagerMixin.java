package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin {
    @WrapOperation(method = "checkRemoteRemap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;containsKey(Lnet/minecraft/resources/ResourceLocation;)Z"))
    private static <T> boolean interceptMissingPotions(Registry<T> instance, ResourceLocation id, Operation<Boolean> original) {
        if (((MappedRegistry<T>) (Object) instance) == BuiltInRegistries.POTION)
            CustomPotionData.tryRegister(id);
        return original.call(instance, id);
    }
}
