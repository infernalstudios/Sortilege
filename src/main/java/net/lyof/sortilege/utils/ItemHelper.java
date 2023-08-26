package net.lyof.sortilege.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.RegistryObject;

public class ItemHelper {
    public static int getEnchantLevel(RegistryObject<Enchantment> enchant, ItemStack item) {
        return getEnchantLevel(enchant.get(), item);
    }

    public static int getEnchantLevel(Enchantment enchant, ItemStack item) {
        return EnchantmentHelper.getTagEnchantmentLevel(enchant, item);
    }

    public static boolean hasEnchant(RegistryObject<Enchantment> enchant, ItemStack item) {
        return hasEnchant(enchant.get(), item);
    }

    public static boolean hasEnchant(Enchantment enchant, ItemStack item) {
        return getEnchantLevel(enchant, item) > 0;
    }
}
