package net.lyof.sortilege.util;

import net.lcc.sollib.core.Identifier;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotionHelper {
    public static final Map<Holder<MobEffect>, Holder<Potion>> POTIONS = new HashMap<>();
    public static final List<Holder<Potion>> GEN_ALLOWED_POTIONS = new ArrayList<>();

    public static void clear() {
        POTIONS.clear();
        GEN_ALLOWED_POTIONS.clear();
    }

    public static void load() {
        for (Holder.Reference<Potion> potion : BuiltInRegistries.POTION.asLookup().listElements().toList()) {
            if (potion.value().getEffects().size() == 1 &&
                    !potion.value().hasInstantEffects() &&
                    potion.value().getEffects().get(0).getAmplifier() == 0 &&
                    !ModConfig.antidoteBlacklist.get().contains(BuiltInRegistries.MOB_EFFECT.getKey(potion.value().getEffects().get(0).getEffect().value()))) {

                Holder<MobEffect> effect = potion.value().getEffects().get(0).getEffect();
                int duration = potion.value().getEffects().get(0).getDuration();

                if (!POTIONS.containsKey(effect))
                    POTIONS.put(effect, potion);
                else if (POTIONS.get(effect).value().getEffects().get(0).getDuration() > duration)
                    POTIONS.replace(effect, potion);
            }
        }

        for (Holder<Potion> potion : POTIONS.values()) {
            if (!ModConfig.swampHutBlacklist.get().contains(Identifier.of(potion.value().getEffects().get(0).getEffect().getRegisteredName())))
                GEN_ALLOWED_POTIONS.add(potion);
        }
    }

    public static Holder<Potion> getDefaultEffect(Holder<MobEffect> effect) {
        return POTIONS.getOrDefault(effect, Potions.WATER);
    }

    public static Holder<Potion> getDefaultPotion(Holder<Potion> potion) {
        return !potion.value().getEffects().isEmpty() ? getDefaultEffect(potion.value().getEffects().get(0).getEffect()) : Potions.WATER;
    }

    public static Holder<Potion> getRandomPotion() {
        return MathHelper.randi(GEN_ALLOWED_POTIONS);
    }

    private static ItemStack effectsCacher = null;
    private static List<MobEffectInstance> effectsCache = null;

    public static List<MobEffectInstance> getEffects(ItemStack stack) {
        if (stack == effectsCacher) return effectsCache;

        if (!stack.has(DataComponents.POTION_CONTENTS)) return List.of();
        List<MobEffectInstance> effects = new ArrayList<>();
        stack.get(DataComponents.POTION_CONTENTS).getAllEffects().forEach(effects::add);

        effectsCache = effects;
        effectsCacher = stack;
        return effects;
    }

    public static String getPotionItemType(ItemStack stack) {
        if (!stack.has(DataComponents.POTION_CONTENTS)) return "";
        ResourceLocation id = Identifier.of(stack.get(DataComponents.POTION_CONTENTS).potion().get().getRegisteredName());

        String base = "";
        if (stack.is(Items.SPLASH_POTION)) base = "/splash";
        else if (stack.is(Items.LINGERING_POTION)) base = "/lingering";

        return id + base;
    }

    public static boolean isPotionItem(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    public static boolean hasEffects(ItemStack stack) {
        return stack.has(DataComponents.POTION_CONTENTS) && stack.get(DataComponents.POTION_CONTENTS).hasEffects();
    }
}
