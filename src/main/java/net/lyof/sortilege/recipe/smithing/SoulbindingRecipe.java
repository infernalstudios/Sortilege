package net.lyof.sortilege.recipe.smithing;

import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.setup.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class SoulbindingRecipe implements SmithingRecipe {
    @Override
    public boolean matches(SmithingRecipeInput inventory, Level world) {
        return this.isTemplateIngredient(inventory.getItem(0)) && this.isBaseIngredient(inventory.getItem(1))
                && this.isAdditionIngredient(inventory.getItem(2));
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput inventory, HolderLookup.Provider lookup) {
        ItemStack stack = inventory.getItem(1).copyWithCount(1);
        stack.enchant(lookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchants.SOULBOUND), 1);
        return stack;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.is(Items.LAPIS_LAZULI);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return stack.is(ModTags.Items.SOULBOUND_BLACKLIST);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return stack.is(ModTags.Items.SOULBINDERS);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SOULBINDING_SERIALIZER;
    }
}
