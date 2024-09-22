package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Map;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow @Final public DefaultedList<ItemStack> main;

    @Redirect(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int skipEquipped(List<ItemStack> list) {
        if ((list != this.main && ConfigEntries.keepEquipped)) {
            return 0;
        }
        return list.size();
    }

    @Redirect(method = "dropAll", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
    public <E> E skipHotbar(List<E> list, int i) {
        if (i < PlayerInventory.getHotbarSize() && ConfigEntries.keepEquipped)
            return (E) ItemStack.EMPTY;

        ItemStack stack = (ItemStack) list.get(i);
        if (ItemHelper.hasEnchant(ModEnchants.SOULBOUND, stack)) {
            if (ConfigEntries.consumeSoulbound) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
                enchants.remove(ModEnchants.SOULBOUND);
            }
            return (E) ItemStack.EMPTY;
        }
        if (stack.isIn(ModTags.Items.KEEP_ON_DEATH)) return (E) ItemStack.EMPTY;

        return list.get(i);
    }
}
