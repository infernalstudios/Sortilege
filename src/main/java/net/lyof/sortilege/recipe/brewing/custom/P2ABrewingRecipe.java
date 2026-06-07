package net.lyof.sortilege.recipe.brewing.custom;

import com.google.gson.JsonObject;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class P2ABrewingRecipe extends BrewingRecipe {
    public P2ABrewingRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return PotionHelper.isPotionItem(stack) && !PotionUtils.getMobEffects(stack).isEmpty();
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.is(Items.GLOW_INK_SAC);
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        List<MobEffectInstance> effects = new ArrayList<>(PotionUtils.getMobEffects(input));
        Collections.shuffle(effects);

        Potion potion = Potions.EMPTY;
        while (potion == Potions.EMPTY && !effects.isEmpty()) {
            if (!effects.get(0).getEffect().isInstantenous())
                potion = PotionHelper.getDefaultPotion(effects.get(0).getEffect());
            if (potion == Potions.EMPTY) effects.remove(0);
        }

        if (potion == Potions.EMPTY) return input;
        return PotionUtils.setPotion(ModItems.ANTIDOTE.getDefaultInstance(), potion);
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
        return PotionUtils.setPotion(Items.POTION.getDefaultInstance(), (Potion) PotionHelper.POTIONS.values().toArray()[i]);
    }

    @Override
    public ItemStack getOutput() {
        return ModItems.ANTIDOTE.getDefaultInstance();
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ANTIDOTE_BREWING_SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<P2ABrewingRecipe> {
        public P2ABrewingRecipe fromJson(ResourceLocation id, JsonObject json) {
            P2ABrewingRecipe recipe = new P2ABrewingRecipe(id);
            BetterBrewingRegistry.register(recipe);
            return recipe;
        }

        public P2ABrewingRecipe fromNetwork(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
            return new P2ABrewingRecipe(identifier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetByteBuf, P2ABrewingRecipe recipe) {}
    }
}
