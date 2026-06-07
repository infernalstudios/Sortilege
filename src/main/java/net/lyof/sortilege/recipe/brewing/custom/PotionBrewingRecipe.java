package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;
import java.util.Random;

public class PotionBrewingRecipe extends BrewingRecipe {
    public Potion input;
    public Item ingredient;
    public Potion output;

    public PotionBrewingRecipe(Potion in, Item add, Potion out, ResourceLocation id) {
        super(id);
        this.input = in;
        this.ingredient = add;
        this.output = out;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return PotionHelper.isPotionItem(stack) && PotionUtils.getPotion(stack) == this.input;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return ItemStack.isSameItem(stack, this.getIngredient());
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        return PotionUtils.setPotion(input.copy(), this.output);
    }


    @Override
    public ItemStack getIngredient() {
        return this.ingredient.getDefaultInstance();
    }

    @Override
    public ItemStack getInput() {
        return PotionUtils.setPotion(Items.POTION.getDefaultInstance(), this.input);
    }

    @Override
    public ItemStack getInput(Random random) {
        return PotionUtils.setPotion(MathHelper.randi(List.of(Items.POTION.getDefaultInstance(),
                Items.SPLASH_POTION.getDefaultInstance(),
                Items.LINGERING_POTION.getDefaultInstance()), random), this.input);
    }

    @Override
    public ItemStack getOutput() {
        return PotionUtils.setPotion(Items.POTION.getDefaultInstance(), this.output);
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.BREWING_SERIALIZER;
    }
}
