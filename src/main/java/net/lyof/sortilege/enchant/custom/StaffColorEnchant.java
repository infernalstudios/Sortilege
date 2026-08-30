package net.lyof.sortilege.enchant.custom;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.ModEnchants;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

public record StaffColorEnchant(List<Integer> colors) {
    public static final Codec<Integer> COLOR_CODEC = Codec.either(Codec.STRING.validate(string ->
                    string.matches("(#)|(0x)[0-9a-fA-F]{6}")
                            ? DataResult.error(() -> "Expected hex formatted number")
                            : DataResult.success(string)
            ).xmap(Integer::decode, String::valueOf), ExtraCodecs.ARGB_COLOR_CODEC)
            .xmap(either -> either.left().isPresent() ? either.left().get() : either.right().get(), Either::left) ;

    public static final Codec<StaffColorEnchant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            COLOR_CODEC.listOf().fieldOf("colors").forGetter(StaffColorEnchant::colors)
    ).apply(instance, StaffColorEnchant::new));
    public static final StreamCodec<FriendlyByteBuf, StaffColorEnchant> STREAM_CODEC = StreamCodec.of(
            (buf, colors) -> {
                buf.writeInt(colors.colors().size());
                colors.colors().forEach(buf::writeInt);
            }, buf -> {
                int size = buf.readInt();
                List<Integer> colors = new ArrayList<>(size);
                for (int i = 0; i < size; i++) colors.add(buf.readInt());
                return new StaffColorEnchant(colors);
            }
    );

    private static ItemEnchantments cacher = null;
    private static List<Integer> cache = null;

    public static List<Integer> collect(ItemEnchantments enchants) {
        if (cacher == enchants) return cache;

        cache = new ArrayList<>();
        for (Object2IntMap.Entry<Holder<Enchantment>> enchant : enchants.entrySet()) {
            StaffColorEnchant colors = enchant.getKey().value().effects().get(ModEnchants.BEAM_COLOR);
            if (colors == null) continue;

            cache.addAll(colors.colors());
        }

        cacher = enchants;
        return cache;
    }
}
