package net.lyof.sortilege.recipe.enchanting.catalyst;

import com.google.gson.JsonObject;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.*;

public class EnchantingCatalyst {
    public static final Map<Item, List<Holder<Enchantment>>> CATALYSTS = new HashMap<>();

    public static void clear() {
        CATALYSTS.clear();
    }

    public static void register(Item catalyst, List<Holder<Enchantment>> enchants) {
        if (CATALYSTS.containsKey(catalyst))
            CATALYSTS.get(catalyst).addAll(enchants);
        else
            CATALYSTS.put(catalyst, enchants);
        // Yeets duplicated entries
        CATALYSTS.replace(catalyst, new ArrayList<>(new HashSet<>(CATALYSTS.get(catalyst))));
    }


    public static boolean isDisabled() {
        return !ModConfig.catalystBooks.get() && CATALYSTS.isEmpty();
    }

    public static ItemEnchantments getEnchantments(ItemStack catalyst) {
        if (catalyst.getItem() instanceof EnchantedBookItem && ModConfig.catalystBooks.get())
            return catalyst.getEnchantments();

        ItemEnchantments.Mutable result = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Holder<Enchantment> enchant : CATALYSTS.getOrDefault(catalyst.getItem(), List.of()))
            result.set(enchant, 1);
        return result.toImmutable();
    }

    public static boolean isCatalyst(ItemStack item) {
        return !getEnchantments(item).isEmpty();
    }


    public static void read(JsonObject json) {/*
        if (json.has("item") && json.has("enchantments") && json.get("enchantments").isJsonArray()) {
            Item item = BuiltInRegistries.ITEM.get(Identifier.of(json.get("item").getAsString()));
            List<Enchantment> enchants = json.get("enchantments").getAsJsonArray().asList().stream()
                    .map(id -> Registries.ENCHANTMENT..get(Identifier.of(id.getAsString()))).filter(Objects::nonNull).toList();

            register(item, enchants);
        }*/
    }

    public static void write(List<CustomPacketPayload> packets) {
        for (Map.Entry<Item, List<Holder<Enchantment>>> entry : CATALYSTS.entrySet())
            packets.add(new ModPackets.InitializeEnchantPacket(entry.getKey(), entry.getValue()));
    }
}
