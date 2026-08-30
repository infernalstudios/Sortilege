package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Random;

public class A2PBrewingRecipe extends BrewingRecipe {
    @Override
    public boolean isInput(ItemStack stack) {
        return stack.getItem() instanceof AntidotePotionItem &&
                PotionHelper.getEffects(stack).size() == 1;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.is(Items.INK_SAC);
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        Holder<Potion> effect = input.get(DataComponents.POTION_CONTENTS).potion().get();
        return PotionContents.createItemStack(Items.POTION, PotionHelper.getDefaultPotion(effect));
    }


    @Override
    public ItemStack getIngredient() {
        return Items.INK_SAC.getDefaultInstance();
    }

    @Override
    public ItemStack getInput() {
        return ModItems.ANTIDOTE.getDefaultInstance();
    }

    @Override
    public ItemStack getInput(Random random) {
        int i = random.nextInt(PotionHelper.POTIONS.size());
        return PotionContents.createItemStack(ModItems.ANTIDOTE, (Holder<Potion>) PotionHelper.POTIONS.values().toArray()[i]);
    }

    @Override
    public ItemStack getOutput() {
        return Items.POTATO.getDefaultInstance();
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.POTION_BREWING_SERIALIZER;
    }
}
