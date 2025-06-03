package net.lyof.sortilege.recipe.brewing.custom;

import com.google.gson.JsonObject;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.IBetterBrewingRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Random;

public class ItemBrewingRecipe implements IBetterBrewingRecipe {
    @Override
    public boolean isInput(ItemStack stack) {
        return stack.isOf(this.input);
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.isOf(this.ingredient);
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
    public String toString() {
        return "BrewingRecipe{" +
                "input=" + input +
                ", ingredient=" + ingredient +
                ", output=" + output +
                '}';
    }

    public Item input;
    public Item ingredient;
    public Item output;

    public ItemBrewingRecipe(Item in, Item add, Item out) {
        this.input = in;
        this.ingredient = add;
        this.output = out;
    }

    public static void read(JsonObject json) {
        if (json.has("input") && json.has("ingredient") && json.has("output")) {
            Item in = Registries.ITEM.get(new Identifier(json.get("input").getAsString()));
            Item add = Registries.ITEM.get(new Identifier(json.get("ingredient").getAsString()));
            Item out = Registries.ITEM.get(new Identifier(json.get("output").getAsString()));

            BetterBrewingRegistry.register(new ItemBrewingRecipe(in, add, out));
        }
    }
}
