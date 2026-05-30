package net.lyof.sortilege.recipe.smithing;

import com.google.gson.JsonObject;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class LimitBreakRecipe implements SmithingRecipe {
    public final Identifier id;

    public LimitBreakRecipe(Identifier id) {
        this.id = id;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.testTemplate(inventory.getStack(0)) && this.testBase(inventory.getStack(1))
                && this.testAddition(inventory.getStack(2));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        ItemStack stack = inventory.getStack(1).copyWithCount(1);
        EnchantHelper.addExtraEnchantSlot(stack);
        return stack;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        return stack.isOf(Items.LAPIS_LAZULI);
    }

    @Override
    public boolean testBase(ItemStack stack) {
        return stack.getItem().getEnchantability() > 0 && EnchantHelper.getBaseEnchantSlots(stack) != 0
                && EnchantHelper.getExtraEnchantSlots(stack) < ConfigEntries.maxLimitBreak;
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return stack.isIn(ModTags.Items.LIMIT_BREAKER);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.LIMIT_BREAK_SERIALIZER;
    }


    public static class Serializer implements RecipeSerializer<LimitBreakRecipe> {
        public LimitBreakRecipe read(Identifier id, JsonObject json) {
            return new LimitBreakRecipe(id);
        }

        public LimitBreakRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new LimitBreakRecipe(identifier);
        }

        public void write(PacketByteBuf packetByteBuf, LimitBreakRecipe recipe) {}
    }
}
