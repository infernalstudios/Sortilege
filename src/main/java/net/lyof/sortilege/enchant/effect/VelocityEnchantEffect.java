package net.lyof.sortilege.enchant.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lyof.sortilege.Sortilege;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record VelocityEnchantEffect(LevelBasedValue x, LevelBasedValue y, LevelBasedValue z) implements EnchantmentEntityEffect {
    public static final MapCodec<VelocityEnchantEffect> CODEC = Codec.withAlternative(RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("x", LevelBasedValue.constant(0)).forGetter(VelocityEnchantEffect::x),
            LevelBasedValue.CODEC.optionalFieldOf("y", LevelBasedValue.constant(0)).forGetter(VelocityEnchantEffect::y),
            LevelBasedValue.CODEC.optionalFieldOf("z", LevelBasedValue.constant(0)).forGetter(VelocityEnchantEffect::z)
    ).apply(instance, VelocityEnchantEffect::new)), LevelBasedValue.CODEC.listOf(3, 3).xmap(
            list -> new VelocityEnchantEffect(list.get(0), list.get(1), list.get(2)),
            effect -> List.of(effect.x(), effect.y(), effect.z())
    )).fieldOf("vector");

    @Override
    public void apply(ServerLevel world, int level, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        entity.setDeltaMovement(x().calculate(level), y().calculate(level), z().calculate(level));
    }

    @Override
    public MapCodec<VelocityEnchantEffect> codec() {
        return CODEC;
    }
}
