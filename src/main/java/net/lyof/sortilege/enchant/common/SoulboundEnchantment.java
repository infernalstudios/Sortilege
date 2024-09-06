package net.lyof.sortilege.enchant.common;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class SoulboundEnchantment extends Enchantment {
    public SoulboundEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.VANISHABLE, EquipmentSlot.values());
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getMaxCount() == 1;
    }
}
