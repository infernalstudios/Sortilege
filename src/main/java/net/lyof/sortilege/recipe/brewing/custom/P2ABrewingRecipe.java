package net.lyof.sortilege.recipe.brewing.custom;

import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class P2ABrewingRecipe extends BrewingRecipe {
    @Override
    public boolean isInput(ItemStack stack) {
        return PotionHelper.isPotionItem(stack) && !PotionHelper.getEffects(stack).isEmpty();
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.is(Items.GLOW_INK_SAC);
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        List<MobEffectInstance> effects = new ArrayList<>(PotionHelper.getEffects(input));
        Collections.shuffle(effects);

        Holder<Potion> potion = Potions.WATER;
        while (potion == Potions.WATER && !effects.isEmpty()) {
            if (!effects.get(0).getEffect().value().isInstantenous())
                potion = PotionHelper.getDefaultEffect(effects.get(0).getEffect());
            if (potion == Potions.WATER) effects.remove(0);
        }

        if (potion == Potions.WATER) return input;
        return PotionContents.createItemStack(ModItems.ANTIDOTE, potion);
    }

    @Override
    public ItemStack getIngredient() {
        return Items.GLOW_INK_SAC.getDefaultInstance();
    }

    @Override
    public ItemStack getInput() {
        return Items.POTION.getDefaultInstance();
    }

    @Override
    public ItemStack getInput(Random random) {
        int i = random.nextInt(PotionHelper.POTIONS.size());
        return PotionContents.createItemStack(Items.POTION, (Holder<Potion>) PotionHelper.POTIONS.values().toArray()[i]);
    }

    @Override
    public ItemStack getOutput() {
        return ModItems.ANTIDOTE.getDefaultInstance();
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ANTIDOTE_BREWING_SERIALIZER;
    }
}
