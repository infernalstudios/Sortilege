package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.mixin.accessor.StatusEffectInstanceAccessor;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(Potion.class)
public class PotionMixin {
    @Unique private boolean lengthenedEffects = false;

    @ModifyReturnValue(method = "getEffects", at = @At("RETURN"))
    public List<StatusEffectInstance> lengthenEffects(List<StatusEffectInstance> original) {
        if (!this.lengthenedEffects) {
            for (StatusEffectInstance effect : original) {
                ((StatusEffectInstanceAccessor) effect).setDuration(
                        (int) Math.round(effect.getDuration() * ConfigEntries.potionLengthMultiplier));

            }
            this.lengthenedEffects = true;
        }
        return original;
    }
}
