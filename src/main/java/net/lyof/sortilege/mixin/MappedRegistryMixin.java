package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> {/*
    private static class BackedMap {

    }
    @Shadow public abstract HolderOwner<T> holderOwner();

    @Unique private boolean sorti_isPotionRegistry() {
        return ((MappedRegistry<T>) (Object) this) == BuiltInRegistries.POTION;
    }

    @Inject(method = "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    public void getCustom(ResourceLocation id, CallbackInfoReturnable<T> cir) {
        if (!this.sorti_isPotionRegistry()) return;

        Potion potion = CustomPotionData.get(id);
        if (potion != null) cir.setReturnValue((T) potion);
    }

    @Inject(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    public void getCustom(ResourceKey<T> key, CallbackInfoReturnable<T> cir) {
        if (!this.sorti_isPotionRegistry()) return;

        Potion potion = CustomPotionData.get(key.location());
        if (potion != null) cir.setReturnValue((T) potion);
    }

    @Inject(method = "getId", at = @At("HEAD"), cancellable = true)
    public void getCustomId(T value, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!this.sorti_isPotionRegistry()) return;

        ResourceLocation id = CustomPotionData.getId((Potion) value);
        if (id != null) cir.setReturnValue(id);
    }

    @ModifyReturnValue(method = "holders", at = @At("RETURN"))
    public List<Holder.Reference<T>> getCustomEntries(List<Holder.Reference<T>> original) {
        if (!this.sorti_isPotionRegistry()) return original;

        List<Holder.Reference<T>> result = new ArrayList<>(original);
        for (Holder.Reference<T> holder : CustomPotionData.getRegistry(this.holderOwner()))
             result.add(holder);
        return result;
    }*/
}
