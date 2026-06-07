package net.lyof.sortilege.recipe.smithing;

import com.google.gson.JsonObject;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;

public class LimitBreakRecipe implements SmithingRecipe {
    public final ResourceLocation id;

    public LimitBreakRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(Container inventory, Level world) {
        return this.isTemplateIngredient(inventory.getItem(0)) && this.isBaseIngredient(inventory.getItem(1))
                && this.isAdditionIngredient(inventory.getItem(2));
    }

    @Override
    public ItemStack assemble(Container inventory, RegistryAccess registryManager) {
        ItemStack stack = inventory.getItem(1).copyWithCount(1);
        EnchantHelper.addExtraEnchantSlot(stack);
        return stack;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.is(Items.LAPIS_LAZULI);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return stack.getItem().getEnchantmentValue() > 0 && EnchantHelper.getBaseEnchantSlots(stack) != 0
                && EnchantHelper.getExtraEnchantSlots(stack) < ConfigEntries.maxLimitBreak;
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return stack.is(ModTags.Items.LIMIT_BREAKER);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.LIMIT_BREAK_SERIALIZER;
    }


    public static class Serializer implements RecipeSerializer<LimitBreakRecipe> {
        public LimitBreakRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new LimitBreakRecipe(id);
        }

        public LimitBreakRecipe fromNetwork(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
            return new LimitBreakRecipe(identifier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetByteBuf, LimitBreakRecipe recipe) {}
    }
}
