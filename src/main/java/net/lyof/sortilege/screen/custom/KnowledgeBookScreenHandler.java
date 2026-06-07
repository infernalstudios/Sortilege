package net.lyof.sortilege.screen.custom;

import net.lyof.sortilege.screen.ModScreenHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class KnowledgeBookScreenHandler extends AbstractContainerMenu {
    public final ItemStack stack;
    public final Container inventory;

    public KnowledgeBookScreenHandler(int syncId, Inventory inventory, FriendlyByteBuf buf) {
        this(syncId, inventory, buf.readItem());
    }

    public KnowledgeBookScreenHandler(int syncId, Inventory inventory, ItemStack stack) {
        super(ModScreenHandlers.KNOWLEDGE_BOOK, syncId);
        this.stack = stack;
        this.inventory = new SimpleContainer(1);

        this.addSlot(new Slot(this.inventory, 0, 0, 0) {
            @Override
            public boolean mayPickup(Player playerEntity) {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
