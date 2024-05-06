package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.brewing.BetterBrewingRecipe;
import net.lyof.sortilege.brewing.BetterBrewingRegistry;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.screen.BrewingStandScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {
    @Inject(method = "isValidIngredient", at = @At("RETURN"), cancellable = true)
    private static void isIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        for (BetterBrewingRecipe recipe : BetterBrewingRegistry.getAll()) {
            if (recipe.isIngredient(stack)) cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasRecipe", at = @At("RETURN"), cancellable = true)
    private static void hasRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (BetterBrewingRegistry.isRecipe(input, ingredient)) cir.setReturnValue(true);
    }

    @Inject(method = "craft", at = @At("RETURN"), cancellable = true)
    private static void craft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        BetterBrewingRecipe recipe = BetterBrewingRegistry.findRecipe(input, ingredient);
        if (recipe == null) return;

        cir.setReturnValue(recipe.craft(input, ingredient));
    }
}
