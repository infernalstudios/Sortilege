package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {
    @WrapMethod(method = "isIngredient")
    private boolean isIngredient(ItemStack stack, Operation<Boolean> original) {
        return original.call(stack) || BetterBrewingRegistry.isIngredient(stack);
    }

    @WrapMethod(method = "isInput")
    private boolean isInput(ItemStack stack, Operation<Boolean> original) {
        return original.call(stack) || BetterBrewingRegistry.isInput(stack);
    }

    @WrapMethod(method = "hasMix")
    private boolean hasRecipe(ItemStack input, ItemStack ingredient, Operation<Boolean> original) {
        return original.call(input, ingredient) || BetterBrewingRegistry.isRecipe(input, ingredient);
    }

    @WrapMethod(method = "mix")
    private ItemStack craft(ItemStack ingredient, ItemStack input, Operation<ItemStack> original) {
        BrewingRecipe recipe = BetterBrewingRegistry.findRecipe(input, ingredient);
        if (recipe != null) return recipe.craft(input, ingredient);

        return original.call(ingredient, input);
    }
}
