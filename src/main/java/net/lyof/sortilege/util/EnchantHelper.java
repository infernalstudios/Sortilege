package net.lyof.sortilege.util;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantHelper {
    private static final Map<Enchantment, List<ItemStack>> ENCHANT_TARGETS = new HashMap<>();
    private static int ENCHANT_COUNT;

    public static void clear() {
        ENCHLIMIT_CACHE.clear();
        ENCHANT_TARGETS.clear();
        ENCHANT_COUNT = 0;
    }

    public static void load() {
        Thread enchantCaching = new Thread(() -> {
            for (Enchantment enchant : BuiltInRegistries.ENCHANTMENT) {
                List<ItemStack> stacks = new ArrayList<>();
                for (Item item : BuiltInRegistries.ITEM) {
                    ItemStack stack = item.getDefaultInstance();
                    if (enchant.canEnchant(stack)) stacks.add(stack);
                }
                ENCHANT_TARGETS.put(enchant, stacks);

                ENCHANT_COUNT += enchant.getMaxLevel();
            }
        });
        enchantCaching.start();
    }


    public static List<ItemStack> getCompatibleStacks(Enchantment enchant) {
        return ENCHANT_TARGETS.getOrDefault(enchant, List.of());
    }

    public static int getEnchantCount() {
        return ENCHANT_COUNT;
    }


    public static int getEnchantLevel(@Nullable Enchantment enchant, ItemStack item) {
        if (enchant == null) return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(enchant, item);
    }

    public static boolean hasEnchant(Enchantment enchant, ItemStack item) {
        return getEnchantLevel(enchant, item) > 0;
    }


    public static final String ENCHLIMIT_NBT = Sortilege.MOD_ID + "_extra_enchants";
    private static final Map<Item, Integer> ENCHLIMIT_CACHE = new HashMap<>();

    public static int getUsedEnchantSlots(ItemStack stack) {
        int l = 0;
        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet())
            if (!enchant.isCurse() || !ModConfig.cursesAddSlots.get()) l++;
        return l;
    }

    public static int getTotalEnchantSlots(ItemStack stack) {
        if (ENCHLIMIT_CACHE == null) return 0;

        int l = getBaseEnchantSlots(stack);
        if (l >= 0)
            l = l + getExtraEnchantSlots(stack) + getCurseEnchantSlots(stack);
        return l;
    }

    public static int getBaseEnchantSlots(ItemStack stack) {
        if (ENCHLIMIT_CACHE.containsKey(stack.getItem())) return ENCHLIMIT_CACHE.get(stack.getItem());

        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        int default_limit = ModConfig.enchantLimiterDefault.get();
        boolean sum = ModConfig.enchantLimiterMode.get().equals("relative");

        if (ModConfig.enchantLimiterOverrides.get().containsKey(id)) {
            int l = ModConfig.enchantLimiterOverrides.get().get(id);
            l = sum ? l + default_limit : l;
            ENCHLIMIT_CACHE.putIfAbsent(stack.getItem(), l);
            return l;
        }

        for (String str : ModConfig.enchantLimiterOverrides.get().keySet()) {
            if (!str.startsWith("#")) continue;

            TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(str.substring(1)));
            if (stack.is(tag)) {
                int l = ModConfig.enchantLimiterOverrides.get().get(str);
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
        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet()) {
            if (enchant.isCurse() && ModConfig.cursesAddSlots.get()) l++;
        }
        return l;
    }

    public static int getExtraEnchantSlots(ItemStack stack) {
        return stack.hasTag() ? stack.getOrCreateTag().getInt(ENCHLIMIT_NBT) : 0;
    }

    public static ItemStack addExtraEnchantSlot(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int current = tag.getInt(ENCHLIMIT_NBT);

        if (current < ModConfig.maxLimitBreak.get())
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
