package net.lyof.sortilege.mixin;

import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public class BrewingStandMenuPotionSlotMixin {
    @Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
    private static void matches(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if (BetterBrewingRegistry.isInput(stack)) cir.setReturnValue(true);
    }
}