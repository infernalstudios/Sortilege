package net.lyof.sortilege.brewing.custom;

import net.lyof.sortilege.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.brewing.IBetterBrewingRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;

public class BrewingRecipe implements IBetterBrewingRecipe {
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

    public BrewingRecipe(Item in, Item add, Item out) {
        this.input = in;
        this.ingredient = add;
        this.output = out;
    }

    public static void read(Map<String, ?> json) {
        if (json.containsKey("input") && json.containsKey("ingredient") && json.containsKey("output")) {
            Item in = Registries.ITEM.get(new Identifier(String.valueOf(json.get("input"))));
            Item add = Registries.ITEM.get(new Identifier(String.valueOf(json.get("ingredient"))));
            Item out = Registries.ITEM.get(new Identifier(String.valueOf(json.get("output"))));

            BetterBrewingRegistry.register(new BrewingRecipe(in, add, out));
        }
    }
}
