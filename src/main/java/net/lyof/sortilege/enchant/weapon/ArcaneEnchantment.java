package net.lyof.sortilege.enchant.weapon;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.FireAspectEnchantment;
import net.minecraft.entity.EquipmentSlot;

public class ArcaneEnchantment extends Enchantment {
    public ArcaneEnchantment(Rarity pRarity) {
        super(pRarity, EnchantmentTarget.WEAPON, EquipmentSlot.values());
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return !(other instanceof FireAspectEnchantment);
    }
}
