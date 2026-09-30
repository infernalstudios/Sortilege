package net.lyof.sortilege.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentAttributeEffect.class)
public abstract class EnchantmentAttributeEffectMixin {
    @Shadow public abstract ResourceLocation id();

    @Inject(method = "idForSlot", at = @At("HEAD"), cancellable = true)
    private void fixBaseAttributeID(StringRepresentable slot, CallbackInfoReturnable<ResourceLocation> cir) {
        if (this.id().equals(Item.BASE_ATTACK_DAMAGE_ID) || this.id().equals(Item.BASE_ATTACK_SPEED_ID))
            cir.setReturnValue(this.id());
    }
}
