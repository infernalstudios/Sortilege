package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.lyof.sortilege.Sortilege;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> {
    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;

    @Shadow @Nullable public abstract T byId(int id);

    @Shadow @Nullable public abstract ResourceLocation getKey(T value);

    @WrapWithCondition(method = "register(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/MappedRegistry;validateWrite(Lnet/minecraft/resources/ResourceKey;)V"))
    private boolean unfreezePotion(MappedRegistry<T> instance, ResourceKey<T> key) {
        return ((MappedRegistry<T>) (Object) this) != BuiltInRegistries.POTION;
    }

    @WrapOperation(method = "registerIdMapping",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectList;get(I)Ljava/lang/Object;", ordinal = 0))
    private Object registerLenient(ObjectList<Holder.Reference<T>> instance, int id, Operation<Object> original,
                                   ResourceKey<T> key) {
        if (0 <= id && id < this.byId.size() && key.location().equals(this.getKey(this.byId.get(id).value())))
            return null;
        return original.call(instance, id);
    }
}
