package net.lyof.sortilege.recipe;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.smithing.LimitBreakRecipe;
import net.lyof.sortilege.recipe.smithing.SoulbindingRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModRecipeTypes {
    public static void register() {}

    private static <T extends Inventory, R extends Recipe<T>> RecipeType<R> register(String name) {
        return Registry.register(Registries.RECIPE_TYPE, Sortilege.makeID(name), new RecipeType<>() {
            @Override
            public String toString() {
                return Sortilege.MOD_ID + ':' + name;
            }
        });
    }

    private static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String name, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Sortilege.makeID(name), serializer);
    }


    public static RecipeSerializer<SoulbindingRecipe> SOULBINDING_RECIPE_SERIALIZER
            = register("soulbind_smithing", new SoulbindingRecipe.Serializer());

    public static RecipeSerializer<LimitBreakRecipe> LIMIT_BREAK_RECIPE_SERIALIZER
            = register("limitbreak_smithing", new LimitBreakRecipe.Serializer());
}
