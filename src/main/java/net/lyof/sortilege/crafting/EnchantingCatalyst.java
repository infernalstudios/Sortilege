package net.lyof.sortilege.crafting;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class EnchantingCatalyst {
    private static final Map<Item, List<Enchantment>> CATALYSTS = new HashMap<>();

    public static void clear() {
        CATALYSTS.clear();
    }

    public static void register(Item catalyst, List<Enchantment> enchants) {
        if (CATALYSTS.containsKey(catalyst))
            CATALYSTS.get(catalyst).addAll(enchants);
        else
            CATALYSTS.put(catalyst, enchants);
        // Yeets duplicated entries
        CATALYSTS.replace(catalyst, new ArrayList<>(new HashSet<>(CATALYSTS.get(catalyst)).stream().toList()));
    }


    public static boolean isEmpty() {
        return CATALYSTS.isEmpty();
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

    @SuppressWarnings("unchecked")
    public static void read(Map<String, ?> json) {
        if (json.containsKey("item") && json.containsKey("enchantments") && json.get("enchantments") instanceof List l) {
            Item item = Registries.ITEM.get(new Identifier(String.valueOf(json.get("item"))));
            List<Enchantment> enchants = l.stream()
                    .map(id -> Registries.ENCHANTMENT.get(new Identifier(String.valueOf(id)))).filter(Objects::nonNull).toList();

            register(item, enchants);
        }
    }
}
