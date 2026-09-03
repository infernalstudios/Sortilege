package net.lyof.sortilege.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EnchantHelper {
    private static final Map<Enchantment, List<ItemStack>> ENCHANT_TARGETS = new HashMap<>();
    private static int ENCHANT_COUNT;

    public static void clear() {
        ENCHANT_TARGETS.clear();
        ENCHANT_COUNT = 0;
    }

    public static void load() {
        Thread enchantCaching = new Thread(() -> {
            for (Enchantment enchant : Set.<Enchantment>of()) {
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


    public static Iterable<Holder<Item>> getCompatibleStacks(Holder<Enchantment> enchant) {
        return enchant.value().getSupportedItems();
    }

    public static int getEnchantCount() {
        return ENCHANT_COUNT;
    }


    public static int getEnchantLevel(ResourceKey<Enchantment> enchant, ItemStack stack) {
        if (enchant == null) return 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet())
            if (enchant.equals(entry.getKey().unwrapKey().orElse(null)))
                return entry.getIntValue();
        return 0;
    }

    public static int getEnchantLevel(Holder<Enchantment> enchant, ItemStack stack) {
        if (enchant == null) return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(enchant, stack);
    }

    public static boolean hasEnchant(ResourceKey<Enchantment> enchant, ItemStack stack) {
        return getEnchantLevel(enchant, stack) > 0;
    }

    public static boolean hasEnchant(Holder<Enchantment> enchant, ItemStack stack) {
        return getEnchantLevel(enchant, stack) > 0;
    }

    public static <T> T getEffect(DataComponentType<T> type, ItemStack stack) {
        for (Holder<Enchantment> enchant : stack.getEnchantments().keySet()) {
            T effect = enchant.value().effects().get(type);
            if (effect != null) return effect;
        }
        return null;
    }

    public static boolean hasEffect(DataComponentType<?> type, ItemStack stack) {
        return getEffect(type, stack) != null;
    }


    private static ItemStack cacher = null;
    private static int usedSlots;
    private static int totalSlots;

    private static void buildCache(ItemStack stack) {
        cacher = stack;

        usedSlots = 0;
        for (Holder<Enchantment> enchant : stack.getEnchantments().keySet())
            if (!enchant.is(EnchantmentTags.CURSE) || !ModConfig.cursesAddSlots.get()) usedSlots++;

        totalSlots = getBaseEnchantSlots(stack);
        if (totalSlots >= 0) totalSlots += getExtraEnchantSlots(stack) + getCurseEnchantSlots(stack);
    }

    public static int getUsedEnchantSlots(ItemStack stack) {
        if (cacher == stack) return usedSlots;
        buildCache(stack);

        return usedSlots;
    }

    public static int getTotalEnchantSlots(ItemStack stack) {
        if (cacher == stack) return totalSlots;
        buildCache(stack);

        return totalSlots;
    }

    public static int getBaseEnchantSlots(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        int defaultLimit = ModConfig.enchantLimiterDefault.get();
        boolean sum = ModConfig.enchantLimiterMode.get().equals("relative");

        if (ModConfig.enchantLimiterOverrides.get().containsKey(id)) {
            int l = ModConfig.enchantLimiterOverrides.get().get(id);
            l = sum ? l + defaultLimit : l;
            return l;
        }

        for (String str : ModConfig.enchantLimiterOverrides.get().keySet()) {
            if (!str.startsWith("#")) continue;

            TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.of(str.substring(1)));
            if (stack.is(tag)) {
                int l = ModConfig.enchantLimiterOverrides.get().get(str);
                l = sum ? l + defaultLimit : l;
                return l;
            }
        }

        return defaultLimit;
    }

    public static int getCurseEnchantSlots(ItemStack stack) {
        int l = 0;
        for (Holder<Enchantment> enchant : stack.getEnchantments().keySet()) {
            if (enchant.is(EnchantmentTags.CURSE) && ModConfig.cursesAddSlots.get()) l++;
        }
        return l;
    }

    public static int getExtraEnchantSlots(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.LIMIT_BREAK, 0);
    }

    public static ItemStack addExtraEnchantSlot(ItemStack stack) {
        int slots = getExtraEnchantSlots(stack);

        if (slots < ModConfig.maxLimitBreak.get())
            slots++;
        stack.set(ModDataComponents.LIMIT_BREAK, slots);
        return stack;
    }


    public static Component getShiftTooltip() {
        return Component.translatable("tooltip.press_shift.left").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("tooltip.press_shift.center").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("tooltip.press_shift.right").withStyle(ChatFormatting.DARK_GRAY));
    }
}
