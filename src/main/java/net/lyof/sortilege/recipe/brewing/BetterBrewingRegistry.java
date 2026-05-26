package net.lyof.sortilege.recipe.brewing;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BetterBrewingRegistry {
    private static final List<BrewingRecipe> RECIPES = new ArrayList<>();

    public static void clear() {
        RECIPES.clear();
    }

    public static void register(BrewingRecipe recipe) {
        RECIPES.add(recipe);
    }


    public static boolean isIngredient(ItemStack stack) {
        for (BrewingRecipe recipe : RECIPES) {
            if (recipe.isIngredient(stack)) return true;
        }
        return false;
    }

    public static boolean isInput(ItemStack stack) {
        for (BrewingRecipe recipe : RECIPES) {
            if (recipe.isInput(stack)) return true;
        }
        return false;
    }

    public static boolean isRecipe(ItemStack input, ItemStack ingredient) {
        return findRecipe(input, ingredient) != null;
    }

    public static BrewingRecipe findRecipe(ItemStack input, ItemStack ingredient) {
        for (BrewingRecipe recipe : RECIPES) {
            if (recipe.isIngredient(ingredient) && recipe.isInput(input)) return recipe;
        }
        return null;
    }
}
