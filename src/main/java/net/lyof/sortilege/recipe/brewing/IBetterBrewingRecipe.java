package net.lyof.sortilege.recipe.brewing;

import net.minecraft.item.ItemStack;

import java.util.Random;

public interface IBetterBrewingRecipe {
    // Bottom Slots
    boolean isInput(ItemStack stack);
    // Top Slot
    boolean isIngredient(ItemStack stack);

    ItemStack craft(ItemStack input, ItemStack ingredient);

    // For EMI compat
    ItemStack getIngredient();

    ItemStack getInput();

    ItemStack getInput(Random random);

    ItemStack getOutput();
}
