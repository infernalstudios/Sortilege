package net.lyof.sortilege.enchant.custom;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lyof.sortilege.enchant.ModEnchants;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

public record StaffColorsEnchant(List<Integer> colors) {
    public static final Codec<Integer> COLOR_CODEC = Codec.either(Codec.STRING.validate(string ->
                    string.matches("(#)|(0x)[0-9a-fA-F]{6}")
                            ? DataResult.error(() -> "Expected hex formatted number")
                            : DataResult.success(string)
            ).xmap(Integer::decode, String::valueOf), ExtraCodecs.ARGB_COLOR_CODEC)
            .xmap(either -> either.left().isPresent() ? either.left().get() : either.right().get(), Either::left) ;

    public static final Codec<StaffColorsEnchant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            COLOR_CODEC.listOf().fieldOf("colors").forGetter(StaffColorsEnchant::colors)
    ).apply(instance, StaffColorsEnchant::new));
    public static final StreamCodec<FriendlyByteBuf, StaffColorsEnchant> STREAM_CODEC = StreamCodec.of(
            (buf, colors) -> {
                buf.writeInt(colors.colors().size());
                colors.colors().forEach(buf::writeInt);
            }, buf -> {
                int size = buf.readInt();
                List<Integer> colors = new ArrayList<>(size);
                for (int i = 0; i < size; i++) colors.add(buf.readInt());
                return new StaffColorsEnchant(colors);
            }
    );

    private static ItemEnchantments cacher = null;
    private static List<Integer> cache = null;

    public static List<Integer> collect(ItemEnchantments enchants) {
        if (cacher == enchants) return cache;

        cache = new ArrayList<>();
        for (Object2IntMap.Entry<Holder<Enchantment>> enchant : enchants.entrySet()) {
            StaffColorsEnchant colors = enchant.getKey().value().effects().get(ModEnchants.STAFF_COLORS);
            if (colors == null) continue;

            cache.addAll(colors.colors());
        }

        cacher = enchants;
        return cache;
    }
}
