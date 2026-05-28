package net.lyof.sortilege.util;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotionHelper {
    public static final Map<StatusEffect, Potion> POTIONS = new HashMap<>();
    public static final List<Potion> GEN_ALLOWED_POTIONS = new ArrayList<>();

    public static void clear() {
        POTIONS.clear();
        GEN_ALLOWED_POTIONS.clear();
    }

    public static void load() {
        Thread potionMapping = new Thread(() -> {
            for (Potion potion : Registries.POTION) {
                if (potion.getEffects().size() == 1 &&
                        !potion.hasInstantEffect() &&
                        potion.getEffects().get(0).getAmplifier() == 0 &&
                        !ConfigEntries.antidoteBlacklist.contains(Registries.STATUS_EFFECT.getKey(potion.getEffects().get(0).getEffectType()).toString())) {

                    StatusEffect effect = potion.getEffects().get(0).getEffectType();
                    int duration = potion.getEffects().get(0).getDuration();

                    if (!POTIONS.containsKey(effect))
                        POTIONS.put(effect, potion);
                    else if (POTIONS.get(effect).getEffects().get(0).getDuration() > duration)
                        POTIONS.replace(effect, potion);
                }
            }
        });
        potionMapping.start();

        for (Potion potion : POTIONS.values()) {
            if (!ConfigEntries.swampHutBlacklist.contains(Registries.STATUS_EFFECT.getKey(potion.getEffects().get(0).getEffectType()).toString()))
                GEN_ALLOWED_POTIONS.add(potion);
        }
    }

    public static Potion getDefaultPotion(StatusEffect effect) {
        return POTIONS.getOrDefault(effect, Potions.EMPTY);
    }

    public static Potion getDefaultPotion(Potion potion) {
        return !potion.getEffects().isEmpty() ? getDefaultPotion(potion.getEffects().get(0).getEffectType()) : Potions.EMPTY;
    }

    public static Potion getRandomPotion() {
        return MathHelper.randi(GEN_ALLOWED_POTIONS);
    }

    public static String getPotionItemType(ItemStack stack) {
        if (!stack.hasNbt()) return "";
        Identifier id = new Identifier(stack.getNbt().getString("Potion"));

        String base = "";
        if (stack.isOf(Items.SPLASH_POTION)) base = "/splash";
        else if (stack.isOf(Items.LINGERING_POTION)) base = "/lingering";

        return id + base;
    }

    public static boolean isPotionItem(ItemStack stack) {
        return stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION);
    }
}
