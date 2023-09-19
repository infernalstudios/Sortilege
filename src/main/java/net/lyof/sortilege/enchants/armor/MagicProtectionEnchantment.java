package net.lyof.sortilege.enchants.armor;

import net.lyof.sortilege.configs.ConfigEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

public class MagicProtectionEnchantment extends Enchantment {
    public static ConfigEntry<Boolean> COMPATIBLE =
            new ConfigEntry<>("enchantments.magic_protection_protection_compatibility", false);

    public MagicProtectionEnchantment(Rarity pRarity) {
        super(pRarity, EnchantmentCategory.ARMOR, EquipmentSlot.values());
    }

    @Override
    public int getDamageProtection(int pLevel, DamageSource pSource) {
        if (pSource.isMagic())
            return pLevel * 2;
        return 0;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return (!(other instanceof ProtectionEnchantment) || COMPATIBLE.get()) && super.checkCompatibility(other);
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }
}
