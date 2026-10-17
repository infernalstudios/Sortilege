package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistryManager.class)
public class RegistryManagerMixin {
    @WrapOperation(method = "applySnapshot(Lnet/minecraft/core/MappedRegistry;Lnet/neoforged/neoforge/registries/RegistrySnapshot;Ljava/util/Set;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/MappedRegistry;containsKey(Lnet/minecraft/resources/ResourceKey;)Z"))
    private static <T> boolean interceptMissingPotions(MappedRegistry<T> instance, ResourceKey<T> key, Operation<Boolean> original) {
        if (instance == BuiltInRegistries.POTION)
            CustomPotionData.tryRegister(key.location());
        return original.call(instance, key);
    }
}
