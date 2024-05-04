package net.lyof.sortilege.enchants.common;

import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SoulboundEnchantment extends Enchantment {
    public static EnchantmentCategory TOOL = EnchantmentCategory.create("TOOL",
            item -> item.getDefaultInstance().getMaxStackSize() == 1);

    public SoulboundEnchantment() {
        super(Rarity.RARE, TOOL, EquipmentSlot.values());
    }
}
