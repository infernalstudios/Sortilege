package net.lyof.sortilege.screen.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.lyof.sortilege.screen.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class KnowledgeBookScreenHandler extends ScreenHandler {
    private final ItemStack stack;

    public KnowledgeBookScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, buf.readItemStack());
    }

    public KnowledgeBookScreenHandler(int syncId, PlayerInventory inventory, ItemStack stack) {
        super(ModScreenHandlers.KNOWLEDGE_BOOK, syncId);
        this.stack = stack;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public ItemStack getStack() {
        return this.stack;
    }
}
