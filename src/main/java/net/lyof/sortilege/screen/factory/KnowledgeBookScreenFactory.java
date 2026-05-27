package net.lyof.sortilege.screen.factory;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.screen.custom.KnowledgeBookScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class KnowledgeBookScreenFactory implements ExtendedScreenHandlerFactory {
    private final ItemStack stack;

    public KnowledgeBookScreenFactory(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        Sortilege.log("Making buf");
    }

    @Override
    public Text getDisplayName() {
        return this.stack.getName();
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new KnowledgeBookScreenHandler(syncId, inventory);
    }
}
