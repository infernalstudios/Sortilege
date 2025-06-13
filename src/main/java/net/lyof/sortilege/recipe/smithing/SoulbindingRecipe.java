package net.lyof.sortilege.recipe.smithing;

import com.google.gson.JsonObject;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class SoulbindingRecipe implements SmithingRecipe {
    public final Identifier id;

    public SoulbindingRecipe(Identifier id) {
        this.id = id;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.testTemplate(inventory.getStack(0)) && this.testBase(inventory.getStack(1))
                && this.testAddition(inventory.getStack(2));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        ItemStack stack = inventory.getStack(1).copy();
        stack.addEnchantment(ModEnchants.SOULBOUND, 1);
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
        return ModEnchants.SOULBOUND != null && !ItemHelper.hasEnchant(ModEnchants.SOULBOUND, stack)
                && ModEnchants.SOULBOUND.isAcceptableItem(stack);
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return stack.isIn(ModTags.Items.SOULBINDERS);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SOULBINDING_SERIALIZER;
    }


    public static class Serializer implements RecipeSerializer<SoulbindingRecipe> {
        public SoulbindingRecipe read(Identifier id, JsonObject json) {
            return new SoulbindingRecipe(id);
        }

        public SoulbindingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new SoulbindingRecipe(identifier);
        }

        public void write(PacketByteBuf packetByteBuf, SoulbindingRecipe recipe) {}
    }
}
