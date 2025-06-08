package net.lyof.sortilege.recipe.brewing.custom;

import com.google.gson.JsonObject;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class P2ABrewingRecipe extends BrewingRecipe {
    public P2ABrewingRecipe(Identifier id) {
        super(id);
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return PotionHelper.isPotionItem(stack) && !PotionUtil.getPotionEffects(stack).isEmpty();
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
        while (potion == Potions.EMPTY && !effects.isEmpty()) {
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
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ANTIDOTE_BREWING_SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<P2ABrewingRecipe> {
        public P2ABrewingRecipe read(Identifier id, JsonObject json) {
            P2ABrewingRecipe recipe = new P2ABrewingRecipe(id);
            BetterBrewingRegistry.register(recipe);
            return recipe;
        }

        public P2ABrewingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new P2ABrewingRecipe(identifier);
        }

        public void write(PacketByteBuf packetByteBuf, P2ABrewingRecipe recipe) {}
    }
}
