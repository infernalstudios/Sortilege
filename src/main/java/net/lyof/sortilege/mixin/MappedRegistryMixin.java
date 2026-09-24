package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("unchecked")
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> {
    @WrapWithCondition(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/MappedRegistry;validateWrite(Lnet/minecraft/resources/ResourceKey;)V"))
    private boolean unfreezePotion(MappedRegistry<T> instance, ResourceKey<T> key) {
        return ((MappedRegistry<T>) (Object) this) != BuiltInRegistries.POTION;
    }
}
