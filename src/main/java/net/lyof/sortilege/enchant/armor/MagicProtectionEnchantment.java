package net.lyof.sortilege.enchant.armor;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

public class MagicProtectionEnchantment extends Enchantment {
    public MagicProtectionEnchantment(Enchantment.Rarity rarity) {
        super(rarity, EnchantmentCategory.ARMOR, EquipmentSlot.values());
    }

    @Override
    public int getDamageProtection(int level, DamageSource source) {
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC))
            return level * 3;
        return 0;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return (!(other instanceof ProtectionEnchantment prot)
                || prot.type == ProtectionEnchantment.Type.FALL
                || ConfigEntries.magicProtCompatibility)
                && super.checkCompatibility(other);
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }
}
