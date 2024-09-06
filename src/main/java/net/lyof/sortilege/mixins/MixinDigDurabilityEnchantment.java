package net.lyof.sortilege.mixins;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DigDurabilityEnchantment.class)
public class MixinDigDurabilityEnchantment {
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    public void noUnbreakable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isDamageableItem()) cir.setReturnValue(false);
    }
}