package net.lyof.sortilege.recipes;

import net.lyof.sortilege.items.ModItems;
import net.lyof.sortilege.items.custom.potion.AntidotePotionItem;
import net.lyof.sortilege.utils.PotionHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.jetbrains.annotations.NotNull;

public class AntidoteBrewingRecipe implements IBrewingRecipe {
    @Override
    public boolean isInput(ItemStack input) {
        return input.getItem() instanceof PotionItem &&
                !(input.getItem() instanceof AntidotePotionItem) &&
                PotionUtils.getPotion(input) != Potions.EMPTY &&
                PotionUtils.getPotion(input).getEffects().size() == 1;
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.getItem() == Items.GLOW_INK_SAC;
    }

    @Override
    public @NotNull ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (isInput(input) && isIngredient(ingredient)) {
            Potion effect = PotionUtils.getPotion(input);

            return PotionUtils.setPotion(new ItemStack(ModItems.ANTIDOTE.get()), PotionHelper.getDefaultPotion(effect));
        }
        return ItemStack.EMPTY;
    }
}
