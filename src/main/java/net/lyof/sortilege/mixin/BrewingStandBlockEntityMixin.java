package net.lyof.sortilege.mixin;

import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {
    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    public void isValid(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot != 3 && slot != 4) {
            if (BetterBrewingRegistry.isInput(stack)) cir.setReturnValue(true);
        }
    }
}
