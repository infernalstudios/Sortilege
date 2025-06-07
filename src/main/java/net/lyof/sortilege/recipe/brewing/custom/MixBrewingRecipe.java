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

public abstract class MixBrewingRecipe implements BrewingRecipe {
    private final Identifier id;

    public MixBrewingRecipe(Identifier id) {
        this.id = id;
    }


    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.BREWING_SERIALIZER;
    }


    public static class Serializer implements RecipeSerializer<MixBrewingRecipe> {
        public MixBrewingRecipe read(Identifier id, JsonObject json) {
            if (!json.has("input") || !json.has("ingredient") || !json.has("output"))
                return null;
            if (json.get("input").isJsonObject() && json.get("input").getAsJsonObject().has("potion")) {
                Potion in = Registries.POTION.get(new Identifier(json.get("input")
                        .getAsJsonObject().get("potion").getAsString()));
                Item add = Registries.ITEM.get(new Identifier(json.get("ingredient")
                        .getAsJsonObject().get("item").getAsString()));
                Potion out = Registries.POTION.get(new Identifier(json.get("output")
                        .getAsJsonObject().get("potion").getAsString()));

                MixBrewingRecipe recipe = new PotionMixBrewingRecipe(in, add, out, id);
                BetterBrewingRegistry.register(recipe);
                return recipe;
            }

            Item in = Registries.ITEM.get(new Identifier(json.get("input")
                    .getAsJsonObject().get("item").getAsString()));
            Item add = Registries.ITEM.get(new Identifier(json.get("ingredient")
                    .getAsJsonObject().get("item").getAsString()));
            Item out = Registries.ITEM.get(new Identifier(json.get("output")
                    .getAsJsonObject().get("item").getAsString()));

            MixBrewingRecipe recipe = new ItemMixBrewingRecipe(in, add, out, id);
            BetterBrewingRegistry.register(recipe);
            return recipe;
        }

        public MixBrewingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            int type = packetByteBuf.readInt();
            switch (type) {
                case 0:
                    Item ini = Registries.ITEM.get(packetByteBuf.readIdentifier());
                    Item addi = Registries.ITEM.get(packetByteBuf.readIdentifier());
                    Item outi = Registries.ITEM.get(packetByteBuf.readIdentifier());
                    return new ItemMixBrewingRecipe(ini, addi, outi, identifier);
                case 1:
                    Potion inp = Registries.POTION.get(packetByteBuf.readIdentifier());
                    Item addp = Registries.ITEM.get(packetByteBuf.readIdentifier());
                    Potion outp = Registries.POTION.get(packetByteBuf.readIdentifier());
                    return new PotionMixBrewingRecipe(inp, addp, outp, identifier);
                default:
                    return null;
            }
        }

        public void write(PacketByteBuf packetByteBuf, MixBrewingRecipe recipe) {
            if (recipe instanceof ItemMixBrewingRecipe itemRecipe) {
                packetByteBuf.writeInt(0);
                packetByteBuf.writeIdentifier(Registries.ITEM.getId(itemRecipe.input));
                packetByteBuf.writeIdentifier(Registries.ITEM.getId(itemRecipe.ingredient));
                packetByteBuf.writeIdentifier(Registries.ITEM.getId(itemRecipe.output));
            }
            else if (recipe instanceof PotionMixBrewingRecipe potionRecipe) {
                packetByteBuf.writeInt(1);
                packetByteBuf.writeIdentifier(Registries.POTION.getId(potionRecipe.input));
                packetByteBuf.writeIdentifier(Registries.ITEM.getId(potionRecipe.ingredient));
                packetByteBuf.writeIdentifier(Registries.POTION.getId(potionRecipe.output));
            }
        }
    }
}
