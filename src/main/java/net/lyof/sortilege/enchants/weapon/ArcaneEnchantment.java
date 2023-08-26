package net.lyof.sortilege.enchants.weapon;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.FireAspectEnchantment;

public class ArcaneEnchantment extends Enchantment {
    public ArcaneEnchantment(Rarity pRarity) {
        super(pRarity, EnchantmentCategory.WEAPON, EquipmentSlot.values());
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return !(other instanceof FireAspectEnchantment);
    }
}
