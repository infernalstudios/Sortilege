package net.lyof.sortilege.enchant.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lyof.sortilege.enchant.ModEnchants;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record StaffStatsEnchant(LevelBasedValue damage, LevelBasedValue range, LevelBasedValue pierce,
                                LevelBasedValue blast, LevelBasedValue kinesis) {
    public static final Codec<StaffStatsEnchant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("damage", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::damage),
            LevelBasedValue.CODEC.optionalFieldOf("range", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::range),
            LevelBasedValue.CODEC.optionalFieldOf("pierce", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::pierce),
            LevelBasedValue.CODEC.optionalFieldOf("blast", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::blast),
            LevelBasedValue.CODEC.optionalFieldOf("kinesis", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::kinesis)
    ).apply(instance, StaffStatsEnchant::new));

    private static ItemEnchantments cacher = null;
    private static StaffStatsEnchant.Result cache = null;

    public static StaffStatsEnchant.Result collect(ItemEnchantments enchants) {
        if (cacher == enchants) return cache;

        float damage = 0, blast = 0, kinesis = 0;
        int range = 0, pierce = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> enchant : enchants.entrySet()) {
            StaffStatsEnchant increase = enchant.getKey().value().effects().get(ModEnchants.STAFF_STATS);
            if (increase == null) continue;

            damage += increase.getDamage(enchant.getIntValue());
            range += increase.getRange(enchant.getIntValue());
            pierce += increase.getPierce(enchant.getIntValue());
            blast += increase.getBlast(enchant.getIntValue());
            kinesis += increase.getKinesis(enchant.getIntValue());
        }

        cache = new StaffStatsEnchant.Result(damage, range, pierce, blast, kinesis);
        cacher = enchants;
        return cache;
    }

    public float getDamage(int level) {
        return damage().calculate(level);
    }

    public int getRange(int level) {
        return Math.round(range().calculate(level));
    }

    public int getPierce(int level) {
        return Math.round(pierce().calculate(level));
    }

    public float getBlast(int level) {
        return Math.round(blast().calculate(level));
    }

    public float getKinesis(int level) {
        return Math.round(kinesis().calculate(level));
    }

    public record Result(float damage, int range, int pierce, float blast, float kinesis) {}
}
