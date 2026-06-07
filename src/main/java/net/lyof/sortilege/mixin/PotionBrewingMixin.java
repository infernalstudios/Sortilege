package net.lyof.sortilege.mixin;

import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {
    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private static void isIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (BetterBrewingRegistry.isIngredient(stack)) cir.setReturnValue(true);
    }

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private static void hasRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (BetterBrewingRegistry.isRecipe(input, ingredient)) cir.setReturnValue(true);
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private static void craft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        BrewingRecipe recipe = BetterBrewingRegistry.findRecipe(input, ingredient);
        if (recipe == null) return;

        cir.setReturnValue(recipe.craft(input, ingredient));
    }
}
