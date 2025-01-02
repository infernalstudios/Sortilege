package net.lyof.sortilege.util;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemHelper {
    public static final List<Item> ENCHANTABLES = new ArrayList<>();
    public static final List<Item> SOULBINDABLES = new ArrayList<>();

    static {
        for (Item item : Registries.ITEM) {
            if (item.getEnchantability() > 0)
                ENCHANTABLES.add(item);

            if (item.getDefaultStack().getMaxCount() == 1 && !item.isFood()
                    && !(item instanceof BucketItem) && !(item instanceof BlockItem))
                SOULBINDABLES.add(item);
        }
    }


    public static int getEnchantLevel(Enchantment enchant, ItemStack item) {
        return EnchantmentHelper.getLevel(enchant, item);
    }

    public static boolean hasEnchant(Enchantment enchant, ItemStack item) {
        return getEnchantLevel(enchant, item) > 0;
    }


    public static final String ENCHLIMIT_NBT = Sortilege.MOD_ID + "_extra_enchants";
    public static final Map<Item, Integer> ENCHLIMIT_CACHE = new HashMap<>();

    public static int getUsedEnchantSlots(ItemStack stack) {
        int l = 0;
        for (Enchantment enchant : EnchantmentHelper.get(stack).keySet())
            if (!enchant.isCursed() || !ConfigEntries.cursesAddSlots) l++;
        return l;
    }

    public static int getTotalEnchantSlots(ItemStack stack) {
        int l = getBaseEnchantSlots(stack);
        if (l >= 0)
            l = l + getExtraEnchantSlots(stack) + getCurseEnchantSlots(stack);
        return l;
    }

    public static int getBaseEnchantSlots(ItemStack stack) {
        if (ENCHLIMIT_CACHE.containsKey(stack.getItem())) return ENCHLIMIT_CACHE.get(stack.getItem());

        String id = Registries.ITEM.getId(stack.getItem()).toString();

        int default_limit = ConfigEntries.enchantLimiterDefault;
        boolean sum = ConfigEntries.enchantLimiterMode.equals("relative");

        if (ConfigEntries.enchantLimiterOverrides.containsKey(id)) {
            int l = ConfigEntries.enchantLimiterOverrides.get(id).intValue();
            l = sum ? l + default_limit : l;
            ENCHLIMIT_CACHE.putIfAbsent(stack.getItem(), l);
            return l;
        }

        for (String str : ConfigEntries.enchantLimiterOverrides.keySet()) {
            if (!str.startsWith("#")) continue;

            TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, new Identifier(str.substring(1)));
            if (stack.isIn(tag)) {
                int l = ConfigEntries.enchantLimiterOverrides.get(str).intValue();
                l = sum ? l + default_limit : l;
                ENCHLIMIT_CACHE.putIfAbsent(stack.getItem(), l);
                return l;
            }
        }

        ENCHLIMIT_CACHE.putIfAbsent(stack.getItem(), default_limit);
        return default_limit;
    }

    public static int getCurseEnchantSlots(ItemStack stack) {
        int l = 0;
        for (Enchantment enchant : EnchantmentHelper.get(stack).keySet()) {
            if (enchant.isCursed() && ConfigEntries.cursesAddSlots) l++;
        }
        return l;
    }

    public static int getExtraEnchantSlots(ItemStack stack) {
        return stack.hasNbt() ? stack.getOrCreateNbt().getInt(ENCHLIMIT_NBT) : 0;
    }

    public static ItemStack addExtraEnchantSlot(ItemStack stack) {
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
