package net.lyof.sortilege.recipe;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.recipe.brewing.CauldronBrewingRecipe;
import net.lyof.sortilege.recipe.brewing.custom.A2PBrewingRecipe;
import net.lyof.sortilege.recipe.brewing.custom.P2ABrewingRecipe;
import net.lyof.sortilege.recipe.smithing.LimitBreakRecipe;
import net.lyof.sortilege.recipe.smithing.SoulbindingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipeTypes {
    public static void register() {
        try {
            //register("botania_staff_lens", BotaniaStaffLensRecipe.SERIALIZER);
        } catch (Throwable ignored) {}
    }

    private static <T extends RecipeInput, R extends Recipe<T>> RecipeType<R> register(String name) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, Sortilege.MOD.makeID(name), new RecipeType<>() {
            @Override
            public String toString() {
                return Sortilege.MOD_ID + ':' + name;
            }
        });
    }

    private static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String name, S serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Sortilege.MOD.makeID(name), serializer);
    }


    public static RecipeType<CauldronBrewingRecipe> CAULDRON_BREWING = register("cauldron_brewing");

    public static RecipeType<BrewingRecipe> BREWING = register("brewing");


    public static RecipeSerializer<SoulbindingRecipe> SOULBINDING_SERIALIZER
            = register("soulbind_smithing", new UnitRecipeSerializer<>(SoulbindingRecipe::new));

    public static RecipeSerializer<LimitBreakRecipe> LIMIT_BREAK_SERIALIZER
            = register("limitbreak_smithing", new UnitRecipeSerializer<>(LimitBreakRecipe::new));

    public static RecipeSerializer<CauldronBrewingRecipe> CAULDRON_BREWING_SERIALIZER
            = register("cauldron_brewing", new CauldronBrewingRecipe.Serializer());

    public static RecipeSerializer<A2PBrewingRecipe> POTION_BREWING_SERIALIZER
            = register("antidote_to_potion_brewing", new UnitRecipeSerializer<>(A2PBrewingRecipe::new));

    public static RecipeSerializer<P2ABrewingRecipe> ANTIDOTE_BREWING_SERIALIZER
            = register("potion_to_antidote_brewing", new UnitRecipeSerializer<>(P2ABrewingRecipe::new));

    public static RecipeSerializer<BrewingRecipe> BREWING_SERIALIZER
            = register("brewing", new BrewingRecipe.Serializer());
}
