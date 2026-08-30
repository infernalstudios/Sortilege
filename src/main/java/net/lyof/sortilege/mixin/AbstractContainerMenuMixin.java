package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow public abstract ItemStack getCarried();

    @WrapOperation(method = "moveItemStackTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;mayPlace(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean preventStorytoldMove(Slot instance, ItemStack stack, Operation<Boolean> original) {
        /*if (!(instance.container instanceof Inventory) && EnchantHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, stack))
            return false;*/
        return original.call(instance, stack);
    }

    @WrapOperation(method = "doClick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/inventory/Slot;safeTake(IILnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack preventStorytoldDrop(Slot instance, int min, int max, Player player, Operation<ItemStack> original) {
        if (EnchantHelper.hasEffect(ModEnchants.PREVENT_DROP, instance.getItem()))
            return ItemStack.EMPTY;
        return original.call(instance, min, max, player);
    }

    @WrapOperation(method = "doClick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setCarried(Lnet/minecraft/world/item/ItemStack;)V"))
    private void preventStorytoldDrop(AbstractContainerMenu instance, ItemStack stack, Operation<Void> original) {
        if (EnchantHelper.hasEffect(ModEnchants.PREVENT_DROP, this.getCarried()))
            return;
        original.call(instance, stack);
    }

    @WrapOperation(method = "doClick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack preventStorytoldDrop(ItemStack instance, int amount, Operation<ItemStack> original) {
        if (EnchantHelper.hasEffect(ModEnchants.PREVENT_DROP, this.getCarried()))
            return instance;
        return original.call(instance, amount);
    }
}
