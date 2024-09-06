package net.lyof.sortilege.util;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PotionHelper {
    public static final Map<StatusEffect, Potion> POTIONS = new HashMap<>();

    static {
        for (Potion potion : Registries.POTION) {
            if (potion != Potions.EMPTY &&
                    potion.getEffects().size() == 1 &&
                    !potion.hasInstantEffect() &&
                    potion.getEffects().get(0).getAmplifier() == 0 &&
                    !ConfigEntries.antidoteBlacklist.contains(
                            Objects.requireNonNull(Registries.STATUS_EFFECT.getKey(potion.getEffects().get(0).getEffectType())).toString())) {

                StatusEffect effect = potion.getEffects().get(0).getEffectType();
                int duration = potion.getEffects().get(0).getDuration();

                if (!POTIONS.containsKey(effect))
                    POTIONS.put(effect, potion);
                else if (POTIONS.get(effect).getEffects().get(0).getDuration() > duration)
                    POTIONS.replace(effect, potion);
            }
        }
    }

    public static Potion getDefaultPotion(StatusEffect effect) {
        return POTIONS.containsKey(effect) ? POTIONS.get(effect) : new Potion();
    }

    public static Potion getDefaultPotion(Potion potion) {
        return potion != Potions.EMPTY ? getDefaultPotion(potion.getEffects().get(0).getEffectType()) : new Potion();
    }
}
