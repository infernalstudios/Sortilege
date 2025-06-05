package net.lyof.sortilege.recipe.brewing;

import com.google.gson.JsonObject;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.brewing.custom.AntidoteBrewingRecipe;
import net.lyof.sortilege.recipe.brewing.custom.PotionBrewingRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
