package net.lyof.sortilege.screen.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.screen.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class KnowledgeBookScreenHandler extends ScreenHandler {
    public final ItemStack stack;
    public final Inventory inventory;

    public KnowledgeBookScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, buf.readItemStack());
    }

    public KnowledgeBookScreenHandler(int syncId, PlayerInventory inventory, ItemStack stack) {
        super(ModScreenHandlers.KNOWLEDGE_BOOK, syncId);
        this.stack = stack;
        this.inventory = new SimpleInventory(1);

        this.addSlot(new Slot(this.inventory, 0, 0, 0) {
            @Override
            public boolean canTakeItems(PlayerEntity playerEntity) {
                return false;
            }

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });
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
