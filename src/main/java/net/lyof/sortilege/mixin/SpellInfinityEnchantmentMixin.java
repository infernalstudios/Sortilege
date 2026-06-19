package net.lyof.sortilege.mixin;

import net.lyof.sortilege.item.custom.staff.SpellEngineStaffItem;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.internals.SpellInfinityEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpellInfinityEnchantment.class)
public class SpellInfinityEnchantmentMixin {
    @Inject(method = "isEligible", at = @At("HEAD"), cancellable = true)
    private static void isStaffEligible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof SpellEngineStaffItem) cir.setReturnValue(true);
    }
}
