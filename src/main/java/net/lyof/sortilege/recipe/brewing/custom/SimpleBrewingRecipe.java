package net.lyof.sortilege.recipe.brewing.custom;

import com.google.gson.JsonObject;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Random;

public class SimpleBrewingRecipe implements BrewingRecipe {
    @Override
    public String toString() {
        return "BrewingRecipe{" +
                "input=" + input +
                ", ingredient=" + ingredient +
                ", output=" + output +
                ", id=" + id +
                '}';
    }

    public Item input;
    public Item ingredient;
    public Item output;
    private final Identifier id;

    public SimpleBrewingRecipe(Item in, Item add, Item out, Identifier id) {
        this.input = in;
        this.ingredient = add;
        this.output = out;
        this.id = id;
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
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.BREWING_SERIALIZER;
    }


    public static final SimpleBrewingRecipe EMPTY = new SimpleBrewingRecipe(Items.AIR, Items.AIR, Items.AIR, Sortilege.makeID("/brewing/empty")) {
        @Override
        public boolean isIngredient(ItemStack stack) { return false; }

        @Override
        public boolean isInput(ItemStack stack) { return false; }
    };

    public static class Serializer implements RecipeSerializer<SimpleBrewingRecipe> {
        public SimpleBrewingRecipe read(Identifier id, JsonObject json) {
            if (!json.has("input") || !json.has("ingredient") || !json.has("output"))
                return EMPTY;
            if (json.get("input").isJsonObject() && json.get("input").getAsJsonObject().has("potion")) {
                Potion in = Registries.POTION.get(new Identifier(json.get("input")
                        .getAsJsonObject().get("potion").getAsString()));
                Item add = Registries.ITEM.get(new Identifier(json.get("ingredient")
                        .getAsJsonObject().get("item").getAsString()));
                Potion out = Registries.POTION.get(new Identifier(json.get("output")
                        .getAsJsonObject().get("potion").getAsString()));

                BrewingRecipeRegistry.registerPotionRecipe(in, add, out);
                BetterBrewingRegistry.store(in + "/" + add + "/" + out);
                return EMPTY;
            }

            Item in = Registries.ITEM.get(new Identifier(json.get("input")
                    .getAsJsonObject().get("item").getAsString()));
            Item add = Registries.ITEM.get(new Identifier(json.get("ingredient")
                    .getAsJsonObject().get("item").getAsString()));
            Item out = Registries.ITEM.get(new Identifier(json.get("output")
                    .getAsJsonObject().get("item").getAsString()));

            SimpleBrewingRecipe recipe = new SimpleBrewingRecipe(in, add, out, id);
            BetterBrewingRegistry.register(recipe);
            return recipe;
        }

        public SimpleBrewingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            Item in = Registries.ITEM.get(packetByteBuf.readIdentifier());
            Item add = Registries.ITEM.get(packetByteBuf.readIdentifier());
            Item out = Registries.ITEM.get(packetByteBuf.readIdentifier());
            return new SimpleBrewingRecipe(in, add, out, identifier);
        }

        public void write(PacketByteBuf packetByteBuf, SimpleBrewingRecipe recipe) {
            packetByteBuf.writeIdentifier(Registries.ITEM.getId(recipe.input));
            packetByteBuf.writeIdentifier(Registries.ITEM.getId(recipe.ingredient));
            packetByteBuf.writeIdentifier(Registries.ITEM.getId(recipe.output));
        }
    }
}
