package net.lyof.sortilege.screen.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.screen.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class KnowledgeBookScreenHandler extends ScreenHandler {
    public KnowledgeBookScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory);
        Sortilege.log("Hey from buf");
    }

    public KnowledgeBookScreenHandler(int syncId, PlayerInventory inventory) {
        super(ModScreenHandlers.KNOWLEDGE_BOOK, syncId);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
