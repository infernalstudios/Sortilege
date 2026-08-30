package net.lyof.sortilege.item;

import com.mojang.serialization.Codec;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static void register() {}

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Sortilege.MOD.makeID(name), builder.apply(DataComponentType.builder()).build());
    }

    public static final DataComponentType<Integer> OVERCHARGE = register("overcharge",
            builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DataComponentType<Integer> LIMIT_BREAK = register("limit_break",
            builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DataComponentType<Unit> LEARNABLE = register("learnable",
            builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    public static final DataComponentType<EnchantKnowledge> KNOWLEDGE = register("enchant_knowledge",
            builder -> builder.persistent(EnchantKnowledge.CODEC).networkSynchronized(EnchantKnowledge.STREAM_CODEC));

    public static final DataComponentType<Integer> LAPIS_SHIELD_COOLDOWN = register("lapis_shield_cooldown",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
}
