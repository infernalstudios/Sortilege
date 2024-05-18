package net.lyof.sortilege.mixin;

import net.lyof.sortilege.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.brewing.IBetterBrewingRecipe;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.BrewingStandScreenHandler$PotionSlot")
public class BrewingStandScreenHandlerMixin {
    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private static void matches(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        for (IBetterBrewingRecipe recipe : BetterBrewingRegistry.getAll()) {
            if (recipe.isInput(stack)) cir.setReturnValue(true);
        }
    }
}