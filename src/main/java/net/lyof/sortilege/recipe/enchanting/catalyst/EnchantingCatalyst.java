package net.lyof.sortilege.recipe.enchanting.catalyst;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

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
            return EnchantmentHelper.getEnchantments(catalyst);

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
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(json.get("item").getAsString()));
            List<Enchantment> enchants = json.get("enchantments").getAsJsonArray().asList().stream()
                    .map(id -> BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(id.getAsString()))).filter(Objects::nonNull).toList();

            register(item, enchants);
        }
    }

    public static void read(FriendlyByteBuf packet) {
        Item key = BuiltInRegistries.ITEM.get(packet.readResourceLocation());
        int enchants = packet.readInt();

        List<Enchantment> value = new ArrayList<>();
        for (int j = 0; j < enchants; j++)
            value.add(BuiltInRegistries.ENCHANTMENT.get(packet.readResourceLocation()));

        CATALYSTS.putIfAbsent(key, value);
    }

    public static void write(List<FriendlyByteBuf> packets) {
        for (Map.Entry<Item, List<Enchantment>> entry : CATALYSTS.entrySet()) {
            FriendlyByteBuf packet = PacketByteBufs.create();
            packet.writeInt(ModPackets.INIT_CATALYST);

            packet.writeResourceLocation(BuiltInRegistries.ITEM.getKey(entry.getKey()));
            packet.writeInt(entry.getValue().size());

            for (Enchantment enchant : entry.getValue())
                packet.writeResourceLocation(BuiltInRegistries.ENCHANTMENT.getKey(enchant));

            packets.add(packet);
        }
    }
}
