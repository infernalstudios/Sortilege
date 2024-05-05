package net.lyof.sortilege.mixins;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Map;

@Mixin(Inventory.class)
public class MixinInventory {
    @Redirect(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int skipEquipped(List<ItemStack> list) {
        Inventory self = (Inventory) (Object) this;
        if (list != self.items && ConfigEntries.keepEquipped) {
            return 0;
        }
        return list.size();
    }

    @Redirect(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
    public <E> E skipHotbar(List<E> list, int i) {
        if (i < Inventory.getSelectionSize() && ConfigEntries.keepEquipped)
            return (E) ItemStack.EMPTY;
        return list.get(i);
    }

    @Redirect(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    public boolean skipSoulbound(ItemStack stack) {
        if (ItemHelper.hasEnchant(ModEnchants.SOULBOUND, stack)) {
            if (ConfigEntries.consumeSoulbound) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
                enchants.remove(ModEnchants.SOULBOUND.get());
            }
            return true;
        }
        return stack.isEmpty();
    }
}
