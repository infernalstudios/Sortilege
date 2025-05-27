package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.mixin.accessor.StatusEffectInstanceAccessor;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Potion.class)
public class PotionMixin {
    @Inject(method = "<init>(Ljava/lang/String;[Lnet/minecraft/entity/effect/StatusEffectInstance;)V", at = @At("TAIL"))
    public void lengthenEffects(String baseName, StatusEffectInstance[] effects, CallbackInfo ci) {
        for (StatusEffectInstance effect : effects) {
            ((StatusEffectInstanceAccessor) effect).setDuration((int) (effect.getDuration() * ConfigEntries.potionLengthMultiplier));
        }
    }
}
