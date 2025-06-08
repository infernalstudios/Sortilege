package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

import java.util.Random;

public class ItemBrewingRecipe extends BrewingRecipe {
    public Item input;
    public Item ingredient;
    public Item output;

    public ItemBrewingRecipe(Item in, Item add, Item out, Identifier id) {
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


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.BREWING_SERIALIZER;
    }
}
