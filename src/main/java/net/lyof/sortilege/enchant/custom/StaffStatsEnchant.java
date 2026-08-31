package net.lyof.sortilege.enchant.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lyof.sortilege.enchant.ModEnchants;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record StaffStatsEnchant(float damage, int range, int pierce) {
    public static final Codec<StaffStatsEnchant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("damage", 0f).forGetter(StaffStatsEnchant::damage),
            Codec.INT.optionalFieldOf("range", 0).forGetter(StaffStatsEnchant::range),
            Codec.INT.optionalFieldOf("pierce", 0).forGetter(StaffStatsEnchant::pierce)
    ).apply(instance, StaffStatsEnchant::new));
    public static final StreamCodec<FriendlyByteBuf, StaffStatsEnchant> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, StaffStatsEnchant::damage,
            ByteBufCodecs.INT, StaffStatsEnchant::range,
            ByteBufCodecs.INT, StaffStatsEnchant::pierce,
            StaffStatsEnchant::new
    );

    private static ItemEnchantments cacher = null;
    private static StaffStatsEnchant cache = null;

    public static StaffStatsEnchant collect(ItemEnchantments enchants) {
        if (cacher == enchants) return cache;

        float damage = 0;
        int range = 0, pierce = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> enchant : enchants.entrySet()) {
            StaffStatsEnchant increase = enchant.getKey().value().effects().get(ModEnchants.STAFF_STATS);
            if (increase == null) continue;

            damage += increase.getDamage(enchant.getIntValue());
            range += increase.getRange(enchant.getIntValue());
            pierce += increase.getPierce(enchant.getIntValue());
        }

        cache = new StaffStatsEnchant(damage, range, pierce);
        cacher = enchants;
        return cache;
    }

    public float getDamage(int level) {
        return damage() * level;
    }

    public int getRange(int level) {
        return range() * level;
    }

    public int getPierce(int level) {
        return pierce() * level;
    }
}
