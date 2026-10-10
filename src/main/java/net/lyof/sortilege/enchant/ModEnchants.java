package net.lyof.sortilege.enchant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.lcc.sollib.api.common.registry.SHolder;
import net.lcc.sollib.api.common.registry.SolModContainer;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.custom.StaffColorsEnchant;
import net.lyof.sortilege.enchant.custom.StaffStatsEnchant;
import net.lyof.sortilege.enchant.effect.FreezeEnchantEffect;
import net.lyof.sortilege.enchant.effect.VelocityEnchantEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@SuppressWarnings("unchecked")
public class ModEnchants {
    public static class EnchantComponentHolder<T> extends SHolder<DataComponentType<T>> {
        public EnchantComponentHolder(SolModContainer mod, String name, Supplier<DataComponentType<T>> entrySupplier) {
            super(mod, name, entrySupplier);
        }

        @Override
        public Registry<DataComponentType<T>> getRegistry() {
            return (Registry<DataComponentType<T>>) (Object) BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE;
        }
    }

    public static class EnchantEffectHolder<T> extends SHolder<MapCodec<T>> {
        public EnchantEffectHolder(SolModContainer mod, String name, Supplier<MapCodec<T>> entrySupplier) {
            super(mod, name, entrySupplier);
        }

        @Override
        public Registry<MapCodec<T>> getRegistry() {
            return (Registry<MapCodec<T>>) (Object) BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE;
        }
    }


    public static void register() {
        register("freeze", FreezeEnchantEffect.CODEC);
        register("apply_velocity", VelocityEnchantEffect.CODEC);
    }

    private static <T> EnchantComponentHolder<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Sortilege.MOD.register(EnchantComponentHolder.class, name, builder.apply(DataComponentType.builder())::build);
    }

    private static <T extends EnchantmentEntityEffect> EnchantEffectHolder<T> register(String name, MapCodec<T> effect) {
        return Sortilege.MOD.register(EnchantEffectHolder.class, name, () -> effect);
    }

    private static ResourceKey<Enchantment> makeKey(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Sortilege.MOD.makeID(name));
    }


    public static final EnchantComponentHolder<StaffStatsEnchant> STAFF_STATS = register("staff_stats",
            builder -> builder.persistent(StaffStatsEnchant.CODEC));
    public static final EnchantComponentHolder<StaffColorsEnchant> STAFF_COLORS = register("staff_colors",
            builder -> builder.persistent(StaffColorsEnchant.CODEC).networkSynchronized(StaffColorsEnchant.STREAM_CODEC));
    public static final EnchantComponentHolder<List<ConditionalEffect<EnchantmentValueEffect>>> STAFF_DAMAGE = register("staff_damage",
            builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf()));
    public static final EnchantComponentHolder<List<TargetedConditionalEffect<EnchantmentEntityEffect>>> ON_STAFF_HIT = register("on_staff_hit",
            builder -> builder.persistent(TargetedConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf()));
    public static final EnchantComponentHolder<List<ConditionalEffect<EnchantmentEntityEffect>>> ON_STAFF_SHOOT = register("on_staff_shoot",
            builder -> builder.persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf()));

    public static final EnchantComponentHolder<List<ConditionalEffect<EnchantmentValueEffect>>> DODGE_CHANCE = register("dodge_chance",
            builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf()));
    public static final EnchantComponentHolder<Holder<DamageType>> DAMAGE_TYPE = register("damage_type",
            builder -> builder.persistent(DamageType.CODEC).networkSynchronized(DamageType.STREAM_CODEC));

    public static final EnchantComponentHolder<Unit> PREVENT_DEATHDROP = register("prevent_deathdrop",
            builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));
    public static final EnchantComponentHolder<Unit> PREVENT_DROP = register("prevent_drop",
            builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));
    public static final EnchantComponentHolder<Integer> TRUE_UNBREAKING = register("true_unbreaking",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));
    public static final EnchantComponentHolder<Integer> TRUE_FIRE_PROTECTION = register("true_fire_protection",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));

    public static final ResourceKey<Enchantment> SOULBOUND = makeKey("soulbound");
}
