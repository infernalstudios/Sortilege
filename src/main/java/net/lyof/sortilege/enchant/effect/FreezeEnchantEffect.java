package net.lyof.sortilege.enchant.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record FreezeEnchantEffect(LevelBasedValue duration, LevelBasedValue wither) implements EnchantmentEntityEffect {
    public static final MapCodec<FreezeEnchantEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(FreezeEnchantEffect::duration),
            LevelBasedValue.CODEC.optionalFieldOf("wither", LevelBasedValue.constant(0)).forGetter(FreezeEnchantEffect::wither)
    ).apply(instance, FreezeEnchantEffect::new));

    @Override
    public void apply(ServerLevel world, int level, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        if (entity.isOnFire() && entity instanceof LivingEntity living)
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, (int) wither().calculate(level)));

        entity.clearFire();
        entity.setTicksFrozen(entity.getTicksFrozen() + (int) duration().calculate(level));
    }

    @Override
    public MapCodec<FreezeEnchantEffect> codec() {
        return CODEC;
    }
}
