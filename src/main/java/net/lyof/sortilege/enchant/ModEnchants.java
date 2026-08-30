package net.lyof.sortilege.enchant;

import com.mojang.serialization.MapCodec;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.custom.StaffColorEnchant;
import net.lyof.sortilege.enchant.custom.StaffStatsEnchant;
import net.lyof.sortilege.enchant.effect.FreezeEnchantEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.function.UnaryOperator;

public class ModEnchants {
    public static void register() {
        register("freeze", FreezeEnchantEffect.CODEC);
    }

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Sortilege.MOD.makeID(name), builder.apply(DataComponentType.builder()).build());
    }

    private static <T extends EnchantmentEntityEffect> MapCodec<T> register(String name, MapCodec<T> effect) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Sortilege.MOD.makeID(name), effect);
    }

    private static ResourceKey<Enchantment> register(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Sortilege.MOD.makeID(name));
    }


    public static final DataComponentType<StaffStatsEnchant> CHANGE_STATS = register("staff_stats",
            builder -> builder.persistent(StaffStatsEnchant.CODEC).networkSynchronized(StaffStatsEnchant.STREAM_CODEC));
    public static final DataComponentType<StaffColorEnchant> BEAM_COLOR = register("staff_beam_colors",
            builder -> builder.persistent(StaffColorEnchant.CODEC).networkSynchronized(StaffColorEnchant.STREAM_CODEC));
    public static final DataComponentType<List<TargetedConditionalEffect<EnchantmentEntityEffect>>> POST_STAFF_HIT = register("post_staff_hit",
            builder -> builder.persistent(TargetedConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf()));
    public static final DataComponentType<Unit> PREVENT_DEATHDROP = register("prevent_deathdrop",
            builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));
    public static final DataComponentType<Unit> PREVENT_DROP = register("prevent_drop",
            builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    public static final ResourceKey<Enchantment> SOULBOUND = register("soulbound");
}
