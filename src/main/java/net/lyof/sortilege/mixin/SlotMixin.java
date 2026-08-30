package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Slot.class)
public class SlotMixin {
    @WrapOperation(method = "safeInsert(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;mayPlace(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean preventStorytoldMove(Slot instance, ItemStack stack, Operation<Boolean> original) {
        if (!(instance.container instanceof Inventory) && EnchantHelper.hasEffect(ModEnchants.PREVENT_DROP, stack))
            return false;
        return original.call(instance, stack);
    }
}
