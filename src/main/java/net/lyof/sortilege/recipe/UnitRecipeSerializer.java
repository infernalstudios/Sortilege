package net.lyof.sortilege.recipe;

import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public record UnitRecipeSerializer<T extends Recipe<?>>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) implements RecipeSerializer<T> {
    public static <T extends Recipe<?>> UnitRecipeSerializer<T> of(Supplier<T> constructor) {
        return new UnitRecipeSerializer<>(MapCodec.unit(constructor),
                StreamCodec.of((buf, recipe) -> {}, buf -> constructor.get()));
    }

    public static <T extends BrewingRecipe> UnitRecipeSerializer<T> ofPotion(Supplier<T> constructor) {
        return new UnitRecipeSerializer<>(MapCodec.of(Encoder.empty(), Decoder.unit(() -> {
            T r = constructor.get();
            BetterBrewingRegistry.register(r);
            return r;
        })), StreamCodec.of((buf, recipe) -> {}, buf -> constructor.get()));
    }
}
