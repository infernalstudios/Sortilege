package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Random;

public class PotionBrewingRecipe extends BrewingRecipe {
    public Potion input;
    public Item ingredient;
    public Potion output;

    public PotionBrewingRecipe(Potion in, Item add, Potion out, Identifier id) {
        super(id);
        this.input = in;
        this.ingredient = add;
        this.output = out;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return PotionHelper.isPotionItem(stack) && PotionUtil.getPotion(stack) == this.input;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return ItemStack.areItemsEqual(stack, this.getIngredient());
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        return PotionUtil.setPotion(input.copy(), this.output);
    }


    @Override
    public ItemStack getIngredient() {
        return this.ingredient.getDefaultStack();
    }

    @Override
    public ItemStack getInput() {
        return PotionUtil.setPotion(Items.POTION.getDefaultStack(), this.input);
    }

    @Override
    public ItemStack getInput(Random random) {
        return PotionUtil.setPotion(MathHelper.randi(List.of(Items.POTION.getDefaultStack(),
                Items.SPLASH_POTION.getDefaultStack(),
                Items.LINGERING_POTION.getDefaultStack()), random), this.input);
    }

    @Override
    public ItemStack getOutput() {
        return PotionUtil.setPotion(Items.POTION.getDefaultStack(), this.output);
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.BREWING_SERIALIZER;
    }
}
