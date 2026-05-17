package net.lyof.sortilege.recipe.enchanting.catalyst;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class EnchantingCatalyst {
    public static final Map<Item, List<Enchantment>> CATALYSTS = new HashMap<>();

    public static void clear() {
        CATALYSTS.clear();
    }

    public static void register(Item catalyst, List<Enchantment> enchants) {
        if (CATALYSTS.containsKey(catalyst))
            CATALYSTS.get(catalyst).addAll(enchants);
        else
            CATALYSTS.put(catalyst, enchants);
        // Yeets duplicated entries
        CATALYSTS.replace(catalyst, new ArrayList<>(new HashSet<>(CATALYSTS.get(catalyst))));
    }


    public static boolean isDisabled() {
        return !ConfigEntries.bookCatalysts && CATALYSTS.isEmpty();
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack catalyst) {
        if (catalyst.getItem() instanceof EnchantedBookItem && ConfigEntries.bookCatalysts)
            return EnchantmentHelper.get(catalyst);

        Map<Enchantment, Integer> result = new HashMap<>();
        for (Enchantment enchant : CATALYSTS.getOrDefault(catalyst.getItem(), List.of()))
            result.put(enchant, 1);
        return result;
    }

    public static boolean isCatalyst(ItemStack item) {
        return !getEnchantments(item).isEmpty();
    }


    public static void read(JsonObject json) {
        if (json.has("item") && json.has("enchantments") && json.get("enchantments").isJsonArray()) {
            Item item = Registries.ITEM.get(new Identifier(json.get("item").getAsString()));
            List<Enchantment> enchants = json.get("enchantments").getAsJsonArray().asList().stream()
                    .map(id -> Registries.ENCHANTMENT.get(new Identifier(id.getAsString()))).filter(Objects::nonNull).toList();

            register(item, enchants);
        }
    }

    public static void read(PacketByteBuf packet) {
        Item key = Registries.ITEM.get(packet.readIdentifier());
        int enchants = packet.readInt();

        List<Enchantment> value = new ArrayList<>();
        for (int j = 0; j < enchants; j++)
            value.add(Registries.ENCHANTMENT.get(packet.readIdentifier()));

        CATALYSTS.putIfAbsent(key, value);
    }

    public static void write(List<PacketByteBuf> packets) {
        for (Map.Entry<Item, List<Enchantment>> entry : CATALYSTS.entrySet()) {
            PacketByteBuf packet = PacketByteBufs.create();
            packet.writeInt(1);

            packet.writeIdentifier(Registries.ITEM.getId(entry.getKey()));
            packet.writeInt(entry.getValue().size());

            for (Enchantment enchant : entry.getValue())
                packet.writeIdentifier(Registries.ENCHANTMENT.getId(enchant));

            packets.add(packet);
        }
    }
}
