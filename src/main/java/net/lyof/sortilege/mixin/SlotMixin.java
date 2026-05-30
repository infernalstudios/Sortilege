package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Slot.class)
public class SlotMixin {
    @WrapOperation(method = "insertStack(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;canInsert(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean preventStorytoldMove(Slot instance, ItemStack stack, Operation<Boolean> original) {
        if (!(instance.inventory instanceof PlayerInventory) && EnchantHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, stack))
            return false;
        return original.call(instance, stack);
    }
}
