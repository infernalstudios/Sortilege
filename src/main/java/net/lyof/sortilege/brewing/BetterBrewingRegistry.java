package net.lyof.sortilege.brewing;

import net.lyof.sortilege.brewing.custom.AntidoteBrewingRecipe;
import net.lyof.sortilege.brewing.custom.PotionBrewingRecipe;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BetterBrewingRegistry {
    public static void register() {
        register(new AntidoteBrewingRecipe());
        register(new PotionBrewingRecipe());
    }


    private static List<IBetterBrewingRecipe> RECIPES = new ArrayList<>();

    public static List<IBetterBrewingRecipe> getAll() {
        return RECIPES;
    }

    public static void clear() {
        RECIPES.clear();
    }

    public static void register(IBetterBrewingRecipe recipe) {
        RECIPES.add(recipe);
    }


    public static boolean isRecipe(ItemStack input, ItemStack ingredient) {
        return findRecipe(input, ingredient) != null;
    }

    public static IBetterBrewingRecipe findRecipe(ItemStack input, ItemStack ingredient) {
        for (IBetterBrewingRecipe recipe : BetterBrewingRegistry.getAll()) {
            if (recipe.isIngredient(ingredient) && recipe.isInput(input)) return recipe;
        }
        return null;
    }
}
