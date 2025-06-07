package net.lyof.sortilege.recipe;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.recipe.brewing.CauldronBrewingRecipe;
import net.lyof.sortilege.recipe.brewing.custom.AntidoteBrewingRecipe;
import net.lyof.sortilege.recipe.brewing.custom.MixBrewingRecipe;
import net.lyof.sortilege.recipe.brewing.custom.PotionBrewingRecipe;
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


    public static RecipeType<CauldronBrewingRecipe> CAULDRON_BREWING = register("cauldron_brewing");

    public static RecipeType<BrewingRecipe> BREWING = register("brewing");


    public static RecipeSerializer<SoulbindingRecipe> SOULBINDING_SERIALIZER
            = register("soulbind_smithing", new SoulbindingRecipe.Serializer());

    public static RecipeSerializer<LimitBreakRecipe> LIMIT_BREAK_SERIALIZER
            = register("limitbreak_smithing", new LimitBreakRecipe.Serializer());

    public static RecipeSerializer<CauldronBrewingRecipe> CAULDRON_BREWING_SERIALIZER
            = register("cauldron_brewing", new CauldronBrewingRecipe.Serializer());

    public static RecipeSerializer<PotionBrewingRecipe> POTION_BREWING_SERIALIZER
            = register("antidote_to_potion_brewing", new PotionBrewingRecipe.Serializer());

    public static RecipeSerializer<AntidoteBrewingRecipe> ANTIDOTE_BREWING_SERIALIZER
            = register("potion_to_antidote_brewing", new AntidoteBrewingRecipe.Serializer());

    public static RecipeSerializer<MixBrewingRecipe> BREWING_SERIALIZER
            = register("brewing", new MixBrewingRecipe.Serializer());
}
