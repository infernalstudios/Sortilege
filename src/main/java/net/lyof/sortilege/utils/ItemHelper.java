package net.lyof.sortilege.utils;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ItemHelper {
    public static final Item LIMIT_BREAKER = Items.ACACIA_BOAT;

    public static final List<Item> ENCHANTABLES = new ArrayList<>();
    public static final List<Item> SOULBINDABLES = new ArrayList<>();

    static {
        for (Item item : Registries.ITEM) {
            if (item.getEnchantability() > 0)
                ENCHANTABLES.add(item);

            if (item.getDefaultStack().getMaxCount() == 1 && !item.isFood() && !(item instanceof BucketItem))
                SOULBINDABLES.add(item);
        }
    }


    public static int getEnchantLevel(Enchantment enchant, ItemStack item) {
        return EnchantmentHelper.getLevel(enchant, item);
    }

    public static boolean hasEnchant(Enchantment enchant, ItemStack item) {
        return getEnchantLevel(enchant, item) > 0;
    }


    public static final String ENCHLIMIT_PATH = "enchantments.enchant_limiter.";
    public static final String ENCHLIMIT_NBT = Sortilege.MOD_ID + "_extra_enchants";

    public static int getMaxEnchantValue(ItemStack stack) {
        String id = Registries.ITEM.getId(stack.getItem()).toString();

        int default_limit = ConfigEntries.enchantLimiterDefault;
        boolean sum = ConfigEntries.enchantLimiterMode.equals("relative");

        int limit = ConfigEntries.enchantLimiterOverrides.getOrDefault(id, sum ? 0.0 : -1.0).intValue();
        //int limit = new ConfigEntry<>(ENCHLIMIT_PATH + "overrides." + id, sum ? 0 : -1).get();
        if (sum) limit += default_limit;
        
        if (limit == -1)
            return default_limit;
        return limit + getExtraEnchants(stack);
    }

    public static int getExtraEnchants(ItemStack stack) {
        return stack.hasNbt() ? stack.getOrCreateNbt().getInt(ENCHLIMIT_NBT) : 0;
    }

    public static ItemStack addExtraEnchant(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateNbt();
        int current = tag.getInt(ENCHLIMIT_NBT);

        if (current < ConfigEntries.maxLimitBreak)
            tag.putInt(ENCHLIMIT_NBT, current + 1);
        stack.setNbt(tag);
        return stack;
    }

    public static Text getShiftTooltip() {
        return Text.translatable("tooltip.press_shift.left").formatted(Formatting.DARK_GRAY)
                .append(Text.translatable("tooltip.press_shift.center").formatted(Formatting.GRAY))
                .append(Text.translatable("tooltip.press_shift.right").formatted(Formatting.DARK_GRAY));
    }
}
