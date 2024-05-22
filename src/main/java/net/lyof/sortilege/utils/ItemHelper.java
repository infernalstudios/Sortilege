package net.lyof.sortilege.utils;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.items.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
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
    public static final List<Item> SOULBINDABLES = new ArrayList<>();

    static {
        for (Item item : Registry.ITEM) {
            if (item.getEnchantmentValue(item.getDefaultInstance()) > 0)
                ENCHANTABLES.add(item);

            if (item.getDefaultInstance().getMaxStackSize() == 1 && !item.isEdible() && !(item instanceof BucketItem))
                SOULBINDABLES.add(item);
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

    public static int getEnchantValue(ItemStack stack) {
        int l = 0;
        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet())
            if (!enchant.isCurse()) l++;
        return l;
    }

    public static int getMaxEnchantValue(ItemStack stack) {
        String id = Registry.ITEM.getKey(stack.getItem()).toString();

        int default_limit = ConfigEntries.enchantLimiterDefault;
        boolean sum = ConfigEntries.enchantLimiterMode.equals("relative");

        int limit = ConfigEntries.enchantLimiterOverrides.getOrDefault(id, sum ? 0.0 : -1.0).intValue();
        //int limit = new ConfigEntry<>(ENCHLIMIT_PATH + "overrides." + id, sum ? 0 : -1).get();
        if (sum)  limit += default_limit;

        int l = limit + getExtraEnchants(stack);
        if (limit == -1)
            l = default_limit;

        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet())
            if (enchant.isCurse()) l++;

        return l;
    }

    public static int getExtraEnchants(ItemStack stack) {
        return stack.hasTag() ? stack.getOrCreateTag().getInt(ENCHLIMIT_NBT) : 0;
    }

    public static ItemStack addExtraEnchant(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int current = tag.getInt(ENCHLIMIT_NBT);

        if (current < ConfigEntries.maxLimitBreak)
            tag.putInt(ENCHLIMIT_NBT, current + 1);
        stack.setTag(tag);
        return stack;
    }

    public static Component getShiftTooltip() {
        return Component.translatable("tooltip.press_shift.left").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("tooltip.press_shift.center").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("tooltip.press_shift.right").withStyle(ChatFormatting.DARK_GRAY));
    }
}
