package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.Sortilege;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.Random;

public class ItemMixBrewingRecipe extends MixBrewingRecipe {
    public Item input;
    public Item ingredient;
    public Item output;

    public ItemMixBrewingRecipe(Item in, Item add, Item out, Identifier id) {
        super(id);
        this.input = in;
        this.ingredient = add;
        this.output = out;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return ItemStack.areItemsEqual(stack, this.getInput());
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return ItemStack.areItemsEqual(stack, this.getIngredient());
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        return this.output.getDefaultStack();
    }


    @Override
    public ItemStack getIngredient() {
        return this.ingredient.getDefaultStack();
    }

    @Override
    public ItemStack getInput() {
        return this.input.getDefaultStack();
    }

    @Override
    public ItemStack getInput(Random random) {
        return this.getInput();
    }

    @Override
    public ItemStack getOutput() {
        return this.output.getDefaultStack();
    }
}
