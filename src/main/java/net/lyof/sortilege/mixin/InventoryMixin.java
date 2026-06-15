package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(value = Inventory.class, priority = 1001)
public abstract class InventoryMixin {
    @Shadow @Final public NonNullList<ItemStack> items;

    @Shadow public abstract ItemStack getSelected();

    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int skipEquipped(List<ItemStack> list, Operation<Integer> original) {
        if ((list != this.items && ModConfig.keepEquipped.get())) {
            return 0;
        }
        return original.call(list);
    }

    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    public boolean skipHotbar(ItemStack stack, Operation<Boolean> original, @Local(index = 3) int i) {
        if (i < Inventory.getSelectionSize() && ModConfig.keepEquipped.get())
            return true;

        if (EnchantHelper.hasEnchant(ModEnchants.SOULBOUND, stack)) {
            if (ModConfig.consumeSoulbound.get() && ModEnchants.SOULBOUND != null) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
                enchants.remove(ModEnchants.SOULBOUND);
            }
            return true;
        }
        if (stack.is(ModTags.Items.KEEP_ON_DEATH)) return true;

        return original.call(stack);
    }

    @WrapMethod(method = "removeFromSelected")
    private ItemStack preventStorytoldDrop(boolean entireStack, Operation<ItemStack> original) {
        if (EnchantHelper.hasEnchant(ModEnchants.STORYTELLING_CURSE, this.getSelected()))
            return ItemStack.EMPTY;
        return original.call(entireStack);
    }
}
