package net.lyof.sortilege.recipe.smithing;

import com.google.gson.JsonObject;
import net.lyof.sortilege.enchant.ModEnchants;
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

public class SoulbindingRecipe implements SmithingRecipe {
    public final ResourceLocation id;

    public SoulbindingRecipe(ResourceLocation id) {
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
        stack.enchant(ModEnchants.SOULBOUND, 1);
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
        return ModEnchants.SOULBOUND != null && !EnchantHelper.hasEnchant(ModEnchants.SOULBOUND, stack)
                && ModEnchants.SOULBOUND.canEnchant(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return stack.is(ModTags.Items.SOULBINDERS);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SOULBINDING_SERIALIZER;
    }


    public static class Serializer implements RecipeSerializer<SoulbindingRecipe> {
        public SoulbindingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new SoulbindingRecipe(id);
        }

        public SoulbindingRecipe fromNetwork(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
            return new SoulbindingRecipe(identifier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetByteBuf, SoulbindingRecipe recipe) {}
    }
}
