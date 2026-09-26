package net.lyof.sortilege.recipe.brewing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.custom.ItemBrewingRecipe;
import net.lyof.sortilege.recipe.brewing.custom.PotionBrewingRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
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


    public static class Serializer implements RecipeSerializer<BrewingRecipe> {
        private static final MapCodec<BrewingRecipe> codec = MapCodec.assumeMapUnsafe(Codec.withAlternative(
                RecordCodecBuilder.create(instance -> instance.group(
                        ItemBrewingRecipe.ITEM_CODEC.fieldOf("input").forGetter(recipe -> ((ItemBrewingRecipe) recipe).input),
                        ItemBrewingRecipe.ITEM_CODEC.fieldOf("ingredient").forGetter(recipe -> ((ItemBrewingRecipe) recipe).ingredient),
                        ItemBrewingRecipe.ITEM_CODEC.fieldOf("output").forGetter(recipe -> ((ItemBrewingRecipe) recipe).output)
                ).apply(instance, (a, b, c) -> {
                    BrewingRecipe r = new ItemBrewingRecipe(a, b, c);
                    BetterBrewingRegistry.register(r);
                    return r;
                })),
                RecordCodecBuilder.create(instance -> instance.group(
                        PotionBrewingRecipe.POTION_CODEC.fieldOf("input").forGetter(recipe -> ((PotionBrewingRecipe) recipe).input),
                        ItemBrewingRecipe.ITEM_CODEC.fieldOf("ingredient").forGetter(recipe -> ((PotionBrewingRecipe) recipe).ingredient),
                        PotionBrewingRecipe.POTION_CODEC.fieldOf("output").forGetter(recipe -> ((PotionBrewingRecipe) recipe).output)
                ).apply(instance, (a, b, c) -> {
                    BrewingRecipe r = new PotionBrewingRecipe(a, b, c);
                    BetterBrewingRegistry.register(r);
                    return r;
                }))
        ));
        private static final StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> streamCodec =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        public static BrewingRecipe fromNetwork(RegistryFriendlyByteBuf packet) {
            int type = packet.readInt();
            switch (type) {
                case 0: {
                    Item ini = BuiltInRegistries.ITEM.get(packet.readResourceLocation());
                    Item addi = BuiltInRegistries.ITEM.get(packet.readResourceLocation());
                    Item outi = BuiltInRegistries.ITEM.get(packet.readResourceLocation());
                    return new ItemBrewingRecipe(ini, addi, outi);
                }
                case 1: {
                    Holder<Potion> inp = BuiltInRegistries.POTION.getHolder(packet.readResourceLocation()).get();
                    Item addp = BuiltInRegistries.ITEM.get(packet.readResourceLocation());
                    Holder<Potion> outp = BuiltInRegistries.POTION.getHolder(packet.readResourceLocation()).get();
                    return new PotionBrewingRecipe(inp, addp, outp);
                } default: return null;
            }
        }

        public static void toNetwork(RegistryFriendlyByteBuf packet, BrewingRecipe recipe) {
            if (recipe instanceof ItemBrewingRecipe itemRecipe) {
                packet.writeInt(0);
                packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(itemRecipe.input));
                packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(itemRecipe.ingredient));
                packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(itemRecipe.output));
            }
            else if (recipe instanceof PotionBrewingRecipe potionRecipe) {
                packet.writeInt(1);
                packet.writeResourceLocation(Identifier.of(potionRecipe.input.getRegisteredName()));
                packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(potionRecipe.ingredient));
                packet.writeResourceLocation(Identifier.of(potionRecipe.output.getRegisteredName()));
            }
        }

        @Override
        public MapCodec<BrewingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
