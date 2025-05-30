package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.recipe.brewing.IBetterBrewingRecipe;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

import java.util.Random;

public class PotionBrewingRecipe implements IBetterBrewingRecipe {
    @Override
    public boolean isInput(ItemStack stack) {
        return stack.getItem() instanceof AntidotePotionItem &&
                PotionUtil.getPotion(stack) != Potions.EMPTY &&
                PotionUtil.getPotion(stack).getEffects().size() == 1;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.isOf(Items.INK_SAC);
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        Potion effect = PotionUtil.getPotion(input);
        return PotionUtil.setPotion(Items.POTION.getDefaultStack(), PotionHelper.getDefaultPotion(effect));
    }

    @Override
    public ItemStack getIngredient() {
        return Items.INK_SAC.getDefaultStack();
    }

    @Override
    public ItemStack getInput() {
        return ModItems.ANTIDOTE.getDefaultStack();
    }

    @Override
    public ItemStack getInput(Random random) {
        int i = random.nextInt(PotionHelper.POTIONS.size());
        return PotionUtil.setPotion(ModItems.ANTIDOTE.getDefaultStack(), (Potion) PotionHelper.POTIONS.values().toArray()[i]);
    }

    @Override
    public ItemStack getOutput() {
        return Items.POTATO.getDefaultStack();
    }

    @Override
    public String toString() {
        return "Antidote to Potion";
    }
}
