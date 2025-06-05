package net.lyof.sortilege.item.custom.potion;

import net.minecraft.entity.effect.StatusEffect;

public interface PotionShenanigans {
    void sorti$setImmunity(StatusEffect effect, int time);
    void sorti$resetPotionCache();
}
