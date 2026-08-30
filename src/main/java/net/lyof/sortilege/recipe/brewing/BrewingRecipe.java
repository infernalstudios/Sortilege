package net.lyof.sortilege.recipe.brewing;

import com.mojang.serialization.MapCodec;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Random;
import java.util.stream.Stream;

public abstract class BrewingRecipe implements Recipe<RecipeInput> {
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
    public boolean matches(RecipeInput inventory, Level world) {
        // 0, 1, 2: Input/Output - 3: Ingredient
        return this.isIngredient(inventory.getItem(3)) && Stream.of(0, 1, 2).anyMatch(i ->
                this.isInput(inventory.getItem(i)));
    }

    @Override
    public ItemStack assemble(RecipeInput inventory, HolderLookup.Provider lookup) {
        ItemStack input, ingredient;
        for (int i = 0; i < 3; i++) {
            input = inventory.getItem(i);
            ingredient = inventory.getItem(3);
            if (this.isInput(input) && this.isIngredient(ingredient))
                return this.craft(input, ingredient);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.getOutput();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.BREWING;
    }


    public static class Serializer implements RecipeSerializer<BrewingRecipe> {/*
        public BrewingRecipe fromJson(ResourceLocation id, JsonObject json) {
            if (!json.has("input") || !json.has("ingredient") || !json.has("output"))
                return null;
            if (json.get("input").isJsonObject() && json.get("input").getAsJsonObject().has("potion")) {
                Potion in = BuiltInRegistries.POTION.get(Identifier.of(json.get("input")
                        .getAsJsonObject().get("potion").getAsString()));
                Item add = BuiltInRegistries.ITEM.get(Identifier.of(json.get("ingredient")
                        .getAsJsonObject().get("item").getAsString()));
                Potion out = BuiltInRegistries.POTION.get(Identifier.of(json.get("output")
                        .getAsJsonObject().get("potion").getAsString()));

                BrewingRecipe recipe = new PotionBrewingRecipe(in, add, out, id);
                BetterBrewingRegistry.register(recipe);
                return recipe;
            }

            Item in = BuiltInRegistries.ITEM.get(Identifier.of(json.get("input")
                    .getAsJsonObject().get("item").getAsString()));
            Item add = BuiltInRegistries.ITEM.get(Identifier.of(json.get("ingredient")
                    .getAsJsonObject().get("item").getAsString()));
            Item out = BuiltInRegistries.ITEM.get(Identifier.of(json.get("output")
                    .getAsJsonObject().get("item").getAsString()));

            BrewingRecipe recipe = new ItemBrewingRecipe(in, add, out, id);
            BetterBrewingRegistry.register(recipe);
            return recipe;
        }

        public BrewingRecipe fromNetwork(ResourceLocation identifier, FriendlyByteBuf packet) {
            int type = packet.readInt();
            switch (type) {
                case 0:
                    Item ini = BuiltInRegistries.ITEM.get(packet.readResourceLocation());
                    Item addi = BuiltInRegistries.ITEM.get(packet.readResourceLocation());
                    Item outi = BuiltInRegistries.ITEM.get(packet.readResourceLocation());
                    return new ItemBrewingRecipe(ini, addi, outi, identifier);
                case 1:
                    Potion inp = BuiltInRegistries.POTION.get(packet.readResourceLocation());
                    Item addp = BuiltInRegistries.ITEM.get(packet.readResourceLocation());
                    Potion outp = BuiltInRegistries.POTION.get(packet.readResourceLocation());
                    return new PotionBrewingRecipe(inp, addp, outp, identifier);
                default:
                    return null;
            }
        }

        @Override
        public void toNetwork(FriendlyByteBuf packet, BrewingRecipe recipe) {
            if (recipe instanceof ItemBrewingRecipe itemRecipe) {
                packet.writeInt(0);
                packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(itemRecipe.input));
                packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(itemRecipe.ingredient));
                packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(itemRecipe.output));
            }
            else if (recipe instanceof PotionBrewingRecipe potionRecipe) {
                packet.writeInt(1);
                packet.writeResourceLocation(BuiltInRegistries.POTION.getKey(potionRecipe.input));
                packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(potionRecipe.ingredient));
                packet.writeResourceLocation(BuiltInRegistries.POTION.getKey(potionRecipe.output));
            }
        }*/

        @Override
        public MapCodec<BrewingRecipe> codec() {
            return null;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> streamCodec() {
            return null;
        }
    }
}
