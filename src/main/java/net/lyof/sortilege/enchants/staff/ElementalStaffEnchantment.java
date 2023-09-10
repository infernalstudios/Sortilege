package net.lyof.sortilege.enchants.staff;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ElementalStaffEnchantment extends StaffEnchantment {
    public ParticleOptions particle;

    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel) {
        this(pRarity, maxLevel, ParticleTypes.INSTANT_EFFECT, null, null);
    }

    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel, ParticleOptions particle, BiConsumer<LivingEntity, Integer> effect) {
        this(pRarity, maxLevel, ParticleTypes.INSTANT_EFFECT, effect, null);
    }

    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel, ParticleOptions particle,
                                     BiConsumer<LivingEntity, Integer> effect, Predicate<Enchantment> condition) {
        super(pRarity, maxLevel, effect, condition);
        this.particle = particle;
    }

    @Override
    protected boolean checkCompatibility(Enchantment candidate) {
        return !(candidate instanceof ElementalStaffEnchantment) && super.checkCompatibility(candidate);
    }
}
