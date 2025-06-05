package net.lyof.sortilege.recipe.brewing.custom;

import com.google.gson.JsonObject;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.recipe.brewing.BrewingRecipe;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

import java.util.Random;

public class PotionBrewingRecipe implements BrewingRecipe {
    public final Identifier id;

    public PotionBrewingRecipe(Identifier id) {
        this.id = id;
    }


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
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.POTION_BREWING_SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<PotionBrewingRecipe> {
        public PotionBrewingRecipe read(Identifier id, JsonObject json) {
            PotionBrewingRecipe recipe = new PotionBrewingRecipe(id);
            BetterBrewingRegistry.register(recipe);
            return recipe;
        }

        public PotionBrewingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new PotionBrewingRecipe(identifier);
        }

        public void write(PacketByteBuf packetByteBuf, PotionBrewingRecipe recipe) {}
    }
}
