package net.lyof.sortilege.enchant.armor;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;

public class MagicProtectionEnchantment extends Enchantment {
    public MagicProtectionEnchantment(Enchantment.Rarity rarity) {
        super(rarity, EnchantmentTarget.ARMOR, EquipmentSlot.values());
    }

    @Override
    public int getProtectionAmount(int level, DamageSource source) {
        if (source.isOf(DamageTypes.MAGIC) || source.isOf(DamageTypes.INDIRECT_MAGIC))
            return level * 2;
        return 0;
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return (!(other instanceof ProtectionEnchantment) || ConfigEntries.magicProtCompatibility) && super.canAccept(other);
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }
}
