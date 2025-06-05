package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {
    @Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
    private static void isIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        for (BrewingRecipe recipe : BetterBrewingRegistry.getAll()) {
            if (recipe.isIngredient(stack)) cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasRecipe", at = @At("HEAD"), cancellable = true)
    private static void hasRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (BetterBrewingRegistry.isRecipe(input, ingredient)) cir.setReturnValue(true);
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private static void craft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        BrewingRecipe recipe = BetterBrewingRegistry.findRecipe(input, ingredient);
        if (recipe == null) return;

        cir.setReturnValue(recipe.craft(input, ingredient));
    }

    @WrapMethod(method = "registerPotionRecipe")
    private static void avoidDuplicates(Potion input, Item item, Potion output, Operation<Void> original) {
        if (!BetterBrewingRegistry.isStored(input + "/" + item + "/" + output))
            original.call(input, item, output);
    }
}
