package net.lyof.sortilege.recipe.brewing.custom;

import com.google.gson.JsonObject;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Random;

public class A2PBrewingRecipe extends BrewingRecipe {
    public A2PBrewingRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return stack.getItem() instanceof AntidotePotionItem &&
                PotionUtils.getPotion(stack) != Potions.EMPTY &&
                PotionUtils.getPotion(stack).getEffects().size() == 1;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.is(Items.INK_SAC);
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack ingredient) {
        Potion effect = PotionUtils.getPotion(input);
        return PotionUtils.setPotion(Items.POTION.getDefaultInstance(), PotionHelper.getDefaultPotion(effect));
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
        return PotionUtils.setPotion(ModItems.ANTIDOTE.getDefaultInstance(), (Potion) PotionHelper.POTIONS.values().toArray()[i]);
    }

    @Override
    public ItemStack getOutput() {
        return Items.POTATO.getDefaultInstance();
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.POTION_BREWING_SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<A2PBrewingRecipe> {
        public A2PBrewingRecipe fromJson(ResourceLocation id, JsonObject json) {
            A2PBrewingRecipe recipe = new A2PBrewingRecipe(id);
            BetterBrewingRegistry.register(recipe);
            return recipe;
        }

        @Override
        public A2PBrewingRecipe fromNetwork(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
            return new A2PBrewingRecipe(identifier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetByteBuf, A2PBrewingRecipe recipe) {}
    }
}
