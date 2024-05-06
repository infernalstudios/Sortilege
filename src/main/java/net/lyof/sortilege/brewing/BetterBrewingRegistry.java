package net.lyof.sortilege.brewing;

import net.lyof.sortilege.Sortilege;
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


    private static List<BetterBrewingRecipe> RECIPES = new ArrayList<>();

    public static List<BetterBrewingRecipe> getAll() {
        return RECIPES;
    }

    public static void register(BetterBrewingRecipe recipe) {
        RECIPES.add(recipe);
    }

    public static boolean isRecipe(ItemStack input, ItemStack ingredient) {
        return findRecipe(input, ingredient) != null;
    }

    public static BetterBrewingRecipe findRecipe(ItemStack input, ItemStack ingredient) {
        for (BetterBrewingRecipe recipe : BetterBrewingRegistry.getAll()) {
            if (recipe.isIngredient(ingredient) && recipe.isInput(input)) return recipe;
        }
        return null;
    }
}
