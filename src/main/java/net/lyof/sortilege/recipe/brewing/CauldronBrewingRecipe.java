package net.lyof.sortilege.recipe.brewing;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CauldronBrewingRecipe implements Recipe<RecipeInput> {
    public final Ingredient input;
    public final Holder<Potion> output;

    public CauldronBrewingRecipe(Ingredient input, Holder<Potion> output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level world) {
        return this.input.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeInput inventory, HolderLookup.Provider lookup) {
        return this.getResultItem(lookup);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider lookup) {
        return PotionContents.createItemStack(Items.POTION, this.output);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CAULDRON_BREWING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CAULDRON_BREWING;
    }

    public static class Serializer implements RecipeSerializer<CauldronBrewingRecipe> {
        private static final MapCodec<CauldronBrewingRecipe> codec =
        RecordCodecBuilder.mapCodec(instance ->
                instance.group(Ingredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                        Potion.CODEC.fieldOf("output").forGetter(recipe -> recipe.output))
                .apply(instance, CauldronBrewingRecipe::new)
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, CauldronBrewingRecipe> streamCodec =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);


        @Override
        public MapCodec<CauldronBrewingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CauldronBrewingRecipe> streamCodec() {
            return streamCodec;
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, CauldronBrewingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input);
            Potion.STREAM_CODEC.encode(buf, recipe.output);
        }

        public static CauldronBrewingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Holder<Potion> output = Potion.STREAM_CODEC.decode(buf);
            return new CauldronBrewingRecipe(input, output);
        }
    }
}
