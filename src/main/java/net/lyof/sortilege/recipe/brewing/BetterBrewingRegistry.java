package net.lyof.sortilege.recipe.brewing;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BetterBrewingRegistry {
    private static final List<BrewingRecipe> RECIPES = new ArrayList<>();
    private static final List<String> FABRIC_RECIPES = new ArrayList<>();

    public static List<BrewingRecipe> getAll() {
        return RECIPES;
    }

    public static void clear() {
        RECIPES.clear();
    }

    public static void register(BrewingRecipe recipe) {
        RECIPES.add(recipe);
    }

    public static void store(String recipe) {
        FABRIC_RECIPES.add(recipe);
    }

    public static boolean isStored(String recipe) {
        return FABRIC_RECIPES.contains(recipe);
    }


    public static boolean isRecipe(ItemStack input, ItemStack ingredient) {
        return findRecipe(input, ingredient) != null;
    }

    public static BrewingRecipe findRecipe(ItemStack input, ItemStack ingredient) {
        for (BrewingRecipe recipe : BetterBrewingRegistry.getAll()) {
            if (recipe.isIngredient(ingredient) && recipe.isInput(input)) return recipe;
        }
        return null;
    }
}
