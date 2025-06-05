package net.lyof.sortilege.recipe.brewing;

import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public interface BrewingRecipe extends Recipe<SimpleInventory> {
    // Bottom Slots
    boolean isInput(ItemStack stack);
    // Top Slot
    boolean isIngredient(ItemStack stack);

    ItemStack craft(ItemStack input, ItemStack ingredient);


    // EMI compat
    ItemStack getIngredient();

    ItemStack getInput();

    ItemStack getInput(Random random);

    ItemStack getOutput();


    // Vanilla handling
    @Override
    default boolean matches(SimpleInventory inventory, World world) {
        // 0, 1, 2: Input/Output - 3: Ingredient
        return this.isIngredient(inventory.getStack(3)) && Stream.of(0, 1, 2).anyMatch(i ->
                this.isInput(inventory.getStack(i)));
    }

    @Override
    default ItemStack craft(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack input, ingredient;
        for (int i = 0; i < 3; i++) {
            input = inventory.getStack(i);
            ingredient = inventory.getStack(3);
            if (this.isInput(input) && this.isIngredient(ingredient))
                return this.craft(input, ingredient);
        }
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack getOutput(DynamicRegistryManager registryManager) {
        return this.getOutput();
    }

    @Override
    default boolean fits(int width, int height) {
        return true;
    }

    @Override
    default RecipeType<?> getType() {
        return ModRecipeTypes.BREWING;
    }
}
