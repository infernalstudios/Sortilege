package net.lyof.sortilege.utils;

import net.lyof.sortilege.configs.ConfigEntries;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PotionHelper {
    public static final Map<MobEffect, Potion> POTIONS = new HashMap<>();

    static {
        for (Potion potion : Registry.POTION) {
            if (potion != Potions.EMPTY &&
                    potion.getEffects().size() == 1 &&
                    !potion.hasInstantEffects() &&
                    potion.getEffects().get(0).getAmplifier() == 0 &&
                    !ConfigEntries.antidoteBlacklist.contains(
                            Objects.requireNonNull(Registry.MOB_EFFECT.getKey(potion.getEffects().get(0).getEffect())).toString())) {

                MobEffect effect = potion.getEffects().get(0).getEffect();
                int duration = potion.getEffects().get(0).getDuration();

                if (!POTIONS.containsKey(effect))
                    POTIONS.put(effect, potion);
                else if (POTIONS.get(effect).getEffects().get(0).getDuration() > duration)
                    POTIONS.replace(effect, potion);
            }
        }
    }

    public static Potion getDefaultPotion(MobEffect effect) {
        return POTIONS.containsKey(effect) ? POTIONS.get(effect) : new Potion();
    }

    public static Potion getDefaultPotion(Potion potion) {
        return potion != Potions.EMPTY ? getDefaultPotion(potion.getEffects().get(0).getEffect()) : new Potion();
    }
}
