package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

//TODO@Mixin(Sortilege.class)
public class RegistrySyncManagerMixin {
    //@WrapOperation(method = "checkRemoteRemap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;containsKey(Lnet/minecraft/resources/ResourceLocation;)Z"))
    private static <T> boolean interceptMissingPotions(Registry<T> instance, ResourceLocation id, Operation<Boolean> original) {
        if (((MappedRegistry<T>) (Object) instance) == BuiltInRegistries.POTION)
            CustomPotionData.tryRegister(id);
        return original.call(instance, id);
    }
}
