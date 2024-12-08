package net.lyof.sortilege.crafting;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.crafting.brewing.BetterBrewingRegistry;
import net.lyof.sortilege.crafting.brewing.custom.BrewingRecipe;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

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
        CATALYSTS.replace(catalyst, new HashSet<>(CATALYSTS.get(catalyst)).stream().toList());
    }

    public static List<Enchantment> getEnchantments(Item catalyst) {
        return CATALYSTS.getOrDefault(catalyst, List.of());
    }

    public static boolean isCatalyst(Item item) {
        return !getEnchantments(item).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static void read(Map<String, ?> json) {
        if (json.containsKey("item") && json.containsKey("enchantments") && json.get("enchantments") instanceof List l) {
            Item item = Registries.ITEM.get(new Identifier(String.valueOf(json.get("item"))));
            List<Enchantment> enchants = l.stream()
                    .map(id -> Registries.ENCHANTMENT.get(new Identifier(String.valueOf(id)))).toList();

            register(item, enchants);
        }
    }
}
