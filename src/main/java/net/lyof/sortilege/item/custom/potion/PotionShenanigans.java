package net.lyof.sortilege.item.custom.potion;

import net.minecraft.world.effect.MobEffect;

public interface PotionShenanigans {
    void sorti_setImmunity(MobEffect effect, int time);
    void sorti_resetPotionCache();
}
