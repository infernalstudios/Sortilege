package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Random;

public class ItemBrewingRecipe extends BrewingRecipe {
    public Item input;
    public Item ingredient;
    public Item output;

    public ItemBrewingRecipe(Item in, Item add, Item out, ResourceLocation id) {
        super(id);
        this.input = in;
        this.ingredient = add;
        this.output = out;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return ItemStack.isSameItem(stack, this.getInput());
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return ItemStack.isSameItem(stack, this.getIngredient());
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        return this.output.getDefaultInstance();
    }


    @Override
    public ItemStack getIngredient() {
        return this.ingredient.getDefaultInstance();
    }

    @Override
    public ItemStack getInput() {
        return this.input.getDefaultInstance();
    }

    @Override
    public ItemStack getInput(Random random) {
        return this.getInput();
    }

    @Override
    public ItemStack getOutput() {
        return this.output.getDefaultInstance();
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.BREWING_SERIALIZER;
    }
}
