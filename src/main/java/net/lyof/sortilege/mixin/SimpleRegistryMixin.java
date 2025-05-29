package net.lyof.sortilege.mixin;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.mixin.accessor.RegistryEntryReferenceAccessor;
import net.minecraft.potion.Potion;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("all")
@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> {
    @Shadow public abstract RegistryEntryOwner<T> getEntryOwner();

    @Unique private boolean isPotionRegistry() {
        return ((SimpleRegistry<T>) (Object) this) == Registries.POTION;
    }

    @Inject(method = "get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    public void getCustom(Identifier id, CallbackInfoReturnable<T> cir) {
        if (!this.isPotionRegistry()) return;

        Potion potion = CustomPotionData.get(id);
        if (potion != null) cir.setReturnValue((T) potion);
    }

    @Inject(method = "get(Lnet/minecraft/registry/RegistryKey;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    public void getCustom(RegistryKey<T> key, CallbackInfoReturnable<T> cir) {
        if (!this.isPotionRegistry()) return;

        Potion potion = CustomPotionData.get(key.getValue());
        if (potion != null) cir.setReturnValue((T) potion);
    }

    @Inject(method = "getId", at = @At("HEAD"), cancellable = true)
    public void getCustomId(T value, CallbackInfoReturnable<Identifier> cir) {
        if (!this.isPotionRegistry()) return;

        Identifier id = CustomPotionData.getId((Potion) value);
        if (id != null) cir.setReturnValue(id);
    }

    @ModifyReturnValue(method = "getEntries", at = @At("RETURN"))
    public List<RegistryEntry.Reference<T>> getCustomEntries(List<RegistryEntry.Reference<T>> original) {
        if (!this.isPotionRegistry()) return original;

        List<RegistryEntry.Reference<T>> result = new ArrayList<>(original);
        result.addAll(CustomPotionData.getRegistry(this.getEntryOwner()));
        return result;
    }
}
