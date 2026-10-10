package net.lyof.sortilege.enchant.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lyof.sortilege.enchant.ModEnchants;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record StaffStatsEnchant(LevelBasedValue damage, LevelBasedValue range, LevelBasedValue pierce,
                                LevelBasedValue blast, LevelBasedValue kinesis, LevelBasedValue cost,
                                LevelBasedValue cooldown) {
    public static final Codec<StaffStatsEnchant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("damage", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::damage),
            LevelBasedValue.CODEC.optionalFieldOf("range", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::range),
            LevelBasedValue.CODEC.optionalFieldOf("pierce", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::pierce),
            LevelBasedValue.CODEC.optionalFieldOf("blast", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::blast),
            LevelBasedValue.CODEC.optionalFieldOf("kinesis", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::kinesis),
            LevelBasedValue.CODEC.optionalFieldOf("cost", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::cost),
            LevelBasedValue.CODEC.optionalFieldOf("cooldown", LevelBasedValue.constant(0)).forGetter(StaffStatsEnchant::cooldown)
    ).apply(instance, StaffStatsEnchant::new));

    private static ItemStack cacher = null;
    private static StaffStatsEnchant.Result cache = null;

    public static StaffStatsEnchant.Result collect(ItemStack stack) {
        if (cacher == stack) return cache;

        float damage = 0, blast = 0, kinesis = 0, cost = 0, cooldown = 0;
        int range = 0, pierce = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> enchant : stack.getEnchantments().entrySet()) {
            StaffStatsEnchant increase = enchant.getKey().value().effects().get(ModEnchants.STAFF_STATS.get());
            if (increase == null) continue;

            damage += increase.damage().calculate(enchant.getIntValue());
            range += Math.round(increase.range().calculate(enchant.getIntValue()));
            pierce += Math.round(increase.pierce().calculate(enchant.getIntValue()));
            blast += increase.blast().calculate(enchant.getIntValue());
            kinesis += increase.kinesis().calculate(enchant.getIntValue());
            cost += increase.cost().calculate(enchant.getIntValue());
            cooldown += increase.cooldown().calculate(enchant.getIntValue());
        }

        cache = new StaffStatsEnchant.Result(damage, range, pierce, blast, kinesis, cost, cooldown);
        cacher = stack;
        return cache;
    }

    public record Result(float damage, int range, int pierce, float blast, float kinesis, float cost, float cooldown) {}
}
