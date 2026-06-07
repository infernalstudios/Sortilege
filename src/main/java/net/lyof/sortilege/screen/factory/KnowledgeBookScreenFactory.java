package net.lyof.sortilege.screen.factory;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class KnowledgeBookScreenFactory implements ExtendedScreenHandlerFactory {
    private final ItemStack stack;

    public KnowledgeBookScreenFactory(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeItem(this.stack);
    }

    @Override
    public Component getDisplayName() {
        return this.stack.getHoverName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new KnowledgeBookScreenHandler(syncId, inventory, this.stack);
    }
}
