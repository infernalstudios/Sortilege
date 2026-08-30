package net.lyof.sortilege.item.potion;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public interface PotionShenanigans {
    void sorti_setImmunity(Holder<MobEffect> effect, int time);
    void sorti_resetPotionCache();
}
