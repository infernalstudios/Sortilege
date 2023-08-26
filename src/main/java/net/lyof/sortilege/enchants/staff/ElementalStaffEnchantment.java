package net.lyof.sortilege.enchants.staff;

import net.minecraft.world.item.enchantment.Enchantment;

public class ElementalStaffEnchantment extends StaffEnchantment {
    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel) {
        super(pRarity, maxLevel);
    }

    @Override
    protected boolean checkCompatibility(Enchantment candidate) {
        return !(candidate instanceof ElementalStaffEnchantment) && super.checkCompatibility(candidate);
    }
}
