package net.lyof.sortilege.recipe.brewing;

import com.google.gson.JsonObject;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.brewing.custom.AntidoteBrewingRecipe;
import net.lyof.sortilege.recipe.brewing.custom.ItemBrewingRecipe;
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
    public static void register() {
        if (ConfigEntries.antidoteEnabled) {
            register(new AntidoteBrewingRecipe());
            register(new PotionBrewingRecipe());
        }
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

    public static void register(JsonObject json) {
        if (json.has("input") && json.has("ingredient") && json.has("output")) {
            Potion in = Registries.POTION.get(new Identifier(json.get("input").getAsString()));
            Item add = Registries.ITEM.get(new Identifier(json.get("ingredient").getAsString()));
            Potion out = Registries.POTION.get(new Identifier(json.get("output").getAsString()));

            BrewingRecipeRegistry.registerPotionRecipe(in, add, out);
        }
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
