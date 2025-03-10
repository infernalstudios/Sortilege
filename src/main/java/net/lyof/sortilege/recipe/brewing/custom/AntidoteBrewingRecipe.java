package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.potion.AntidotePotionItem;
import net.lyof.sortilege.recipe.brewing.IBetterBrewingRecipe;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AntidoteBrewingRecipe implements IBetterBrewingRecipe {
    @Override
    public boolean isInput(ItemStack stack) {
        return stack.getItem() instanceof PotionItem &&
                !(stack.getItem() instanceof AntidotePotionItem) &&
                PotionUtil.getPotionEffects(stack).size() >= 1;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.isOf(Items.GLOW_INK_SAC);
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(input);
        Collections.shuffle(effects);

        Potion potion = Potions.EMPTY;
        while (potion == Potions.EMPTY && effects.size() > 0) {
            if (!effects.get(0).getEffectType().isInstant())
                potion = PotionHelper.getDefaultPotion(effects.get(0).getEffectType());
            if (potion == Potions.EMPTY) effects.remove(0);
        }

        if (potion == Potions.EMPTY) return input;
        return PotionUtil.setPotion(ModItems.ANTIDOTE.getDefaultStack(), potion);
    }

    @Override
    public ItemStack getIngredient() {
        return Items.GLOW_INK_SAC.getDefaultStack();
    }

    @Override
    public ItemStack getInput() {
        return Items.POTION.getDefaultStack();
    }

    @Override
    public ItemStack getInput(Random random) {
        int i = random.nextInt(PotionHelper.POTIONS.size());
        return PotionUtil.setPotion(Items.POTION.getDefaultStack(), (Potion) PotionHelper.POTIONS.values().toArray()[i]);
    }

    @Override
    public ItemStack getOutput() {
        return ModItems.ANTIDOTE.getDefaultStack();
    }

    @Override
    public String toString() {
        return "Potion to Antidote";
    }
}
