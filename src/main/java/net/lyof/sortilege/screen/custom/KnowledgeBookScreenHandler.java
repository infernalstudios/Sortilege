package net.lyof.sortilege.screen.custom;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.lyof.sortilege.screen.ModScreenHandlers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class KnowledgeBookScreenHandler extends AbstractContainerMenu {
    public static ExtendedScreenHandlerFactory<ItemStack> getFactory(ItemStack stack) {
        return new ExtendedScreenHandlerFactory<>() {
            @Override
            public ItemStack getScreenOpeningData(ServerPlayer player) {
                return stack;
            }

            @Override
            public Component getDisplayName() {
                return stack.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                return new KnowledgeBookScreenHandler(syncId, inventory, stack);
            }
        };
    }

    public final ItemStack stack;
    public final Container inventory;

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
