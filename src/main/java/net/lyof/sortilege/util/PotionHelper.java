package net.lyof.sortilege.util;

import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotionHelper {
    public static final Map<MobEffect, Potion> POTIONS = new HashMap<>();
    public static final List<Potion> GEN_ALLOWED_POTIONS = new ArrayList<>();

    public static void clear() {
        POTIONS.clear();
        GEN_ALLOWED_POTIONS.clear();
    }

    public static void load() {
        Thread potionMapping = new Thread(() -> {
            for (Potion potion : BuiltInRegistries.POTION) {
                if (potion.getEffects().size() == 1 &&
                        !potion.hasInstantEffects() &&
                        potion.getEffects().get(0).getAmplifier() == 0 &&
                        !ModConfig.antidoteBlacklist.get().contains(BuiltInRegistries.MOB_EFFECT.getKey(potion.getEffects().get(0).getEffect()))) {

                    MobEffect effect = potion.getEffects().get(0).getEffect();
                    int duration = potion.getEffects().get(0).getDuration();

                    if (!POTIONS.containsKey(effect))
                        POTIONS.put(effect, potion);
                    else if (POTIONS.get(effect).getEffects().get(0).getDuration() > duration)
                        POTIONS.replace(effect, potion);
                }
            }

            for (Potion potion : POTIONS.values()) {
                if (!ModConfig.swampHutBlacklist.get().contains(BuiltInRegistries.MOB_EFFECT.getKey(potion.getEffects().get(0).getEffect())))
                    GEN_ALLOWED_POTIONS.add(potion);
            }
        });
        potionMapping.start();
    }

    public static Potion getDefaultPotion(MobEffect effect) {
        return POTIONS.getOrDefault(effect, Potions.EMPTY);
    }

    public static Potion getDefaultPotion(Potion potion) {
        return !potion.getEffects().isEmpty() ? getDefaultPotion(potion.getEffects().get(0).getEffect()) : Potions.EMPTY;
    }

    public static Potion getRandomPotion() {
        return MathHelper.randi(GEN_ALLOWED_POTIONS);
    }

    public static String getPotionItemType(ItemStack stack) {
        if (!stack.hasTag()) return "";
        ResourceLocation id = new ResourceLocation(stack.getTag().getString("Potion"));

        String base = "";
        if (stack.is(Items.SPLASH_POTION)) base = "/splash";
        else if (stack.is(Items.LINGERING_POTION)) base = "/lingering";

        return id + base;
    }

    public static boolean isPotionItem(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }
}
