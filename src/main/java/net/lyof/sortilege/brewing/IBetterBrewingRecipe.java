package net.lyof.sortilege.brewing;

import net.minecraft.item.ItemStack;

public interface IBetterBrewingRecipe {
    // Bottom Slots
    boolean isInput(ItemStack stack);
    // Top Slot
    boolean isIngredient(ItemStack stack);

    ItemStack craft(ItemStack input, ItemStack ingredient);
}
