package net.lyof.sortilege.util;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnchantHelper {
    private static Supplier<Registry<Enchantment>> REGISTRY;
    private static int ENCHANT_COUNT;

    public static void setRegistry(Supplier<Registry<Enchantment>> registry) {
        Sortilege.log().info("Set Enchantment registry on", FabricLoader.getInstance().getEnvironmentType());
        REGISTRY = registry;
    }

    public static boolean iterateRegistry(Consumer<Holder<Enchantment>> consumer) {
        Registry<Enchantment> registry = null;
        try {
            registry = REGISTRY.get();
        } catch (Exception ignored) {}
        if (registry == null) return false;

        registry.holders().forEach(consumer);
        return true;
    }

    public static void load() {
        Thread enchantCaching = new Thread(() -> {
            ENCHANT_COUNT = 0;
            iterateRegistry(enchant -> ENCHANT_COUNT += enchant.value().getMaxLevel());
        });
        enchantCaching.start();
    }

    public static int getEnchantCount() {
        return ENCHANT_COUNT;
    }


    @FunctionalInterface public interface LootContextBuilder {
        LootParams build(LootParams.Builder params, int level);
    }
    @FunctionalInterface public interface EnchantConsumer<T> {
        void run(T effect, int level, ServerLevel server);
    }
    @FunctionalInterface public interface TargetedEnchantConsumer<T> {
        void run(T effect, int level, LivingEntity target, ServerLevel server);
    }

    public static <T> void iterateEffects(DataComponentType<List<ConditionalEffect<T>>> type, LivingEntity entity,
                                          LootContextBuilder contextBuilder, EnchantConsumer<T> enchantConsumer) {
        if (!(entity.level() instanceof ServerLevel server)) return;

        ItemStack stack;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stack = entity.getItemBySlot(slot);
            if (stack.isEmpty())
                continue;

            for (Object2IntMap.Entry<Holder<Enchantment>> enchant : stack.getEnchantments().entrySet()) {
                if (!enchant.getKey().value().isSupportedItem(stack))
                    continue;
                if (!enchant.getKey().value().matchingSlot(slot))
                    continue;

                LootContext context = new LootContext.Builder(contextBuilder.build(new LootParams.Builder(server),
                        enchant.getIntValue())).create(Optional.empty());

                for (ConditionalEffect<T> effect : enchant.getKey().value().getEffects(type)) {
                    if (effect.matches(context))
                        enchantConsumer.run(effect.effect(), enchant.getIntValue(), server);
                }
            }
        }
    }

    public static <T> void iterateEffects(DataComponentType<List<TargetedConditionalEffect<T>>> type,
                                          LivingEntity user, LivingEntity target,
                                          LootContextBuilder contextBuilder, TargetedEnchantConsumer<T> enchantConsumer) {
        if (!(user.level() instanceof ServerLevel server)) return;

        ItemStack stack;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stack = user.getItemBySlot(slot);
            if (stack.isEmpty())
                continue;

            for (Object2IntMap.Entry<Holder<Enchantment>> enchant : stack.getEnchantments().entrySet()) {
                if (!enchant.getKey().value().isSupportedItem(stack))
                    continue;
                if (!enchant.getKey().value().matchingSlot(slot))
                    continue;

                LootContext context = new LootContext.Builder(contextBuilder.build(new LootParams.Builder(server),
                        enchant.getIntValue())).create(Optional.empty());

                for (TargetedConditionalEffect<T> effect : enchant.getKey().value().getEffects(type)) {
                    if (effect.matches(context))
                        enchantConsumer.run(effect.effect(), enchant.getIntValue(),
                                effect.affected() == EnchantmentTarget.VICTIM ? target : user,
                                server);
                }
            }
        }
    }

    public static <T> Pair<T, Integer> getEffect(DataComponentType<T> type, ItemStack stack) {
        for (Object2IntMap.Entry<Holder<Enchantment>> enchant : stack.getEnchantments().entrySet()) {
            T effect = enchant.getKey().value().effects().get(type);
            if (effect != null) return new Pair<>(effect, enchant.getIntValue());
        }
        return null;
    }

    public static boolean hasEffect(DataComponentType<?> type, ItemStack stack) {
        return getEffect(type, stack) != null;
    }

    public static <T> boolean hasEffect(DataComponentType<T> type, ItemStack stack, BiPredicate<T, Integer> condition) {
        Pair<T, Integer> effect = getEffect(type, stack);
        return effect != null && condition.test(effect.getFirst(), effect.getSecond());
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
