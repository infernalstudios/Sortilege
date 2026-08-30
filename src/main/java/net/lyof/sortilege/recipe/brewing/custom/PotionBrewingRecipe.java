package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;
import java.util.Random;

public class PotionBrewingRecipe extends BrewingRecipe {
    public Holder<Potion> input;
    public Item ingredient;
    public Holder<Potion> output;

    public PotionBrewingRecipe(Holder<Potion> in, Item add, Holder<Potion> out, ResourceLocation id) {
        this.input = in;
        this.ingredient = add;
        this.output = out;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return PotionHelper.isPotionItem(stack) && stack.get(DataComponents.POTION_CONTENTS).potion().get().is(this.input);
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return ItemStack.isSameItem(stack, this.getIngredient());
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        ItemStack result = input.copy();
        result.set(DataComponents.POTION_CONTENTS, result.get(DataComponents.POTION_CONTENTS).withPotion(this.output));
        return result;
    }


    @Override
    public ItemStack getIngredient() {
        return this.ingredient.getDefaultInstance();
    }

    @Override
    public ItemStack getInput() {
        return PotionContents.createItemStack(Items.POTION, this.input);
    }

    @Override
    public ItemStack getInput(Random random) {
        return PotionContents.createItemStack(MathHelper.randi(List.of(Items.POTION,
                Items.SPLASH_POTION,
                Items.LINGERING_POTION), random), this.input);
    }

    @Override
    public ItemStack getOutput() {
        return PotionContents.createItemStack(Items.POTION, this.output);
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.BREWING_SERIALIZER;
    }
}
