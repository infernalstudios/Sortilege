package net.lyof.sortilege.utils;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.configs.ConfigEntry;
import net.lyof.sortilege.items.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ItemHelper {
    public static final Item LIMIT_BREAKER = ModItems.LIMITITE.get();
    public static final List<Item> ENCHANTABLES = new ArrayList<>();

    static {
        for (Item item : Registry.ITEM) {
            if (item.getEnchantmentValue(new ItemStack((item))) > 0) {
                ENCHANTABLES.add(item);
            }
        }
    }



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


    public static final String ENCHLIMIT_PATH = "enchantments.enchant_limiter.";
    public static final String ENCHLIMIT_NBT = Sortilege.MOD_ID + "_extra_enchants";

    public static int getMaxEnchantValue(ItemStack itemstack) {
        String id = Registry.ITEM.getKey(itemstack.getItem()).toString();

        int default_limit = ConfigEntries.EnchantLimiterDefault;
        boolean sum = ConfigEntries.EnchantLimiterMode.equals("relative");

        int limit = new ConfigEntry<>(ENCHLIMIT_PATH + "overrides." + id, sum ? 0 : -1).get();
        if (sum)  limit += default_limit;
        
        if (limit == -1)
            return default_limit;
        return limit + itemstack.getOrCreateTag().getInt(ENCHLIMIT_NBT);
    }

    public static ItemStack addExtraEnchant(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(ENCHLIMIT_NBT, tag.getInt(ENCHLIMIT_NBT) + 1);
        stack.setTag(tag);
        return stack;
    }

    public static Component getShiftTooltip() {
        return Component.translatable("tooltip.press_shift.left").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("tooltip.press_shift.center").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("tooltip.press_shift.right").withStyle(ChatFormatting.DARK_GRAY));
    }
}
