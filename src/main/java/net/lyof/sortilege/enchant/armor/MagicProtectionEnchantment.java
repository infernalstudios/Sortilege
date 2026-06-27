package net.lyof.sortilege.enchant.armor;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

public class MagicProtectionEnchantment extends Enchantment {
    public static final TagKey<DamageType> IS_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, Identifier.of("c", "is_magic"));

    public MagicProtectionEnchantment(Enchantment.Rarity rarity) {
        super(rarity, EnchantmentCategory.ARMOR, EquipmentSlot.values());
    }

    @Override
    public int getDamageProtection(int level, DamageSource source) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO) || source.is(IS_MAGIC))
            return level * 3;
        return 0;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return (!(other instanceof ProtectionEnchantment prot)
                || prot.type == ProtectionEnchantment.Type.FALL
                || ModConfig.magicProtCompatibility.get())
                && super.checkCompatibility(other);
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }
}
