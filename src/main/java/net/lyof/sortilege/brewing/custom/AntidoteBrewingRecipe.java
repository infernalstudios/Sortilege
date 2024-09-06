package net.lyof.sortilege.brewing.custom;

import net.lyof.sortilege.brewing.IBetterBrewingRecipe;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.potion.AntidotePotionItem;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

public class AntidoteBrewingRecipe implements IBetterBrewingRecipe {
    @Override
    public boolean isInput(ItemStack stack) {
        return stack.getItem() instanceof PotionItem &&
                !(stack.getItem() instanceof AntidotePotionItem) &&
                PotionUtil.getPotion(stack) != Potions.EMPTY &&
                PotionUtil.getPotion(stack).getEffects().size() == 1;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.isOf(Items.GLOW_INK_SAC);
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        Potion effect = PotionUtil.getPotion(input);
        return PotionUtil.setPotion(ModItems.ANTIDOTE.getDefaultStack(), PotionHelper.getDefaultPotion(effect));
    }

    @Override
    public String toString() {
        return "Potion to Antidote";
    }
}
