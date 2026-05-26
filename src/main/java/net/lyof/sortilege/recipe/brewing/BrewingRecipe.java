package net.lyof.sortilege.recipe.brewing;

import com.google.gson.JsonObject;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.custom.ItemBrewingRecipe;
import net.lyof.sortilege.recipe.brewing.custom.PotionBrewingRecipe;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Random;
import java.util.stream.Stream;

public abstract class BrewingRecipe implements Recipe<SimpleInventory> {
    private final Identifier id;

    public BrewingRecipe(Identifier id) {
        this.id = id;
    }


    // Bottom Slots
    public abstract boolean isInput(ItemStack stack);
    // Top Slot
    public abstract boolean isIngredient(ItemStack stack);

    public abstract ItemStack craft(ItemStack input, ItemStack ingredient);


    // EMI compat
    public abstract ItemStack getIngredient();

    public abstract ItemStack getInput();

    public abstract ItemStack getInput(Random random);

    public abstract ItemStack getOutput();


    // Vanilla handling
    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        // 0, 1, 2: Input/Output - 3: Ingredient
        return this.isIngredient(inventory.getStack(3)) && Stream.of(0, 1, 2).anyMatch(i ->
                this.isInput(inventory.getStack(i)));
    }

    @Override
    public ItemStack craft(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack input, ingredient;
        for (int i = 0; i < 3; i++) {
            input = inventory.getStack(i);
            ingredient = inventory.getStack(3);
            if (this.isInput(input) && this.isIngredient(ingredient))
                return this.craft(input, ingredient);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return this.getOutput();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.BREWING;
    }


    public static class Serializer implements RecipeSerializer<BrewingRecipe> {
        public BrewingRecipe read(Identifier id, JsonObject json) {
            if (!json.has("input") || !json.has("ingredient") || !json.has("output"))
                return null;
            if (json.get("input").isJsonObject() && json.get("input").getAsJsonObject().has("potion")) {
                Potion in = Registries.POTION.get(new Identifier(json.get("input")
                        .getAsJsonObject().get("potion").getAsString()));
                Item add = Registries.ITEM.get(new Identifier(json.get("ingredient")
                        .getAsJsonObject().get("item").getAsString()));
                Potion out = Registries.POTION.get(new Identifier(json.get("output")
                        .getAsJsonObject().get("potion").getAsString()));

                BrewingRecipe recipe = new PotionBrewingRecipe(in, add, out, id);
                BetterBrewingRegistry.register(recipe);
                return recipe;
            }

            Item in = Registries.ITEM.get(new Identifier(json.get("input")
                    .getAsJsonObject().get("item").getAsString()));
            Item add = Registries.ITEM.get(new Identifier(json.get("ingredient")
                    .getAsJsonObject().get("item").getAsString()));
            Item out = Registries.ITEM.get(new Identifier(json.get("output")
                    .getAsJsonObject().get("item").getAsString()));

            BrewingRecipe recipe = new ItemBrewingRecipe(in, add, out, id);
            BetterBrewingRegistry.register(recipe);
            return recipe;
        }

        public BrewingRecipe read(Identifier identifier, PacketByteBuf packet) {
            int type = packet.readInt();
            switch (type) {
                case 0:
                    Item ini = Registries.ITEM.get(packet.readIdentifier());
                    Item addi = Registries.ITEM.get(packet.readIdentifier());
                    Item outi = Registries.ITEM.get(packet.readIdentifier());
                    return new ItemBrewingRecipe(ini, addi, outi, identifier);
                case 1:
                    Potion inp = Registries.POTION.get(packet.readIdentifier());
                    Item addp = Registries.ITEM.get(packet.readIdentifier());
                    Potion outp = Registries.POTION.get(packet.readIdentifier());
                    return new PotionBrewingRecipe(inp, addp, outp, identifier);
                default:
                    return null;
            }
        }

        public void write(PacketByteBuf packet, BrewingRecipe recipe) {
            if (recipe instanceof ItemBrewingRecipe itemRecipe) {
                packet.writeInt(0);
                packet.writeIdentifier(Registries.ITEM.getId(itemRecipe.input));
                packet.writeIdentifier(Registries.ITEM.getId(itemRecipe.ingredient));
                packet.writeIdentifier(Registries.ITEM.getId(itemRecipe.output));
            }
            else if (recipe instanceof PotionBrewingRecipe potionRecipe) {
                packet.writeInt(1);
                packet.writeIdentifier(Registries.POTION.getId(potionRecipe.input));
                packet.writeIdentifier(Registries.ITEM.getId(potionRecipe.ingredient));
                packet.writeIdentifier(Registries.POTION.getId(potionRecipe.output));
            }
        }
    }
}
