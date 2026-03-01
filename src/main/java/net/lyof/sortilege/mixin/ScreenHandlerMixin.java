package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow public abstract ItemStack getCursorStack();

    @WrapOperation(method = "insertItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;canInsert(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean preventStorytoldMove(Slot instance, ItemStack stack, Operation<Boolean> original) {
        if (!(instance.inventory instanceof PlayerInventory) && ItemHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, stack))
            return false;
        return original.call(instance, stack);
    }

    @WrapOperation(method = "internalOnSlotClick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/screen/slot/Slot;takeStackRange(IILnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack preventStorytoldDrop(Slot instance, int min, int max, PlayerEntity player, Operation<ItemStack> original) {
        if (ItemHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, instance.getStack()))
            return ItemStack.EMPTY;
        return original.call(instance, min, max, player);
    }

    @WrapOperation(method = "internalOnSlotClick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/screen/ScreenHandler;setCursorStack(Lnet/minecraft/item/ItemStack;)V"))
    private void preventStorytoldDrop(ScreenHandler instance, ItemStack stack, Operation<Void> original) {
        if (ItemHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, this.getCursorStack()))
            return;
        original.call(instance, stack);
    }

    @WrapOperation(method = "internalOnSlotClick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/item/ItemStack;split(I)Lnet/minecraft/item/ItemStack;"))
    private ItemStack preventStorytoldDrop(ItemStack instance, int amount, Operation<ItemStack> original) {
        if (ItemHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, this.getCursorStack()))
            return instance;
        return original.call(instance, amount);
    }
}
