package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.custom.potion.CustomPotionData;
import net.lyof.sortilege.item.custom.potion.IPotionShenanigans;
import net.lyof.sortilege.mixin.accessor.StatusEffectInstanceAccessor;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Potion.class)
public class PotionMixin implements IPotionShenanigans {
    @Unique private List<StatusEffectInstance> customEffects = null;
    @Unique private boolean lengthened = false;

    @Override
    public void sorti$setImmunity(StatusEffect effect, int time) {}

    @Override
    public void sorti$resetPotionCache() {
        this.customEffects = null;
    }

    @ModifyReturnValue(method = "getEffects", at = @At("RETURN"))
    public List<StatusEffectInstance> lengthenEffects(List<StatusEffectInstance> original) {
        if (this.customEffects == null) {
            if (!CustomPotionData.isEmpty()) {
                CustomPotionData data = CustomPotionData.get((Potion) (Object) this);
                if (data == null)
                    this.customEffects = List.of();
                else
                    this.customEffects = data.effects;
            }
        }

        if (!this.lengthened) {
            for (StatusEffectInstance effect : original) {
                ((StatusEffectInstanceAccessor) effect).setDuration(
                        (int) Math.round(effect.getDuration() * ConfigEntries.potionDurationMultiplier));
            }
            this.lengthened = true;
        }

        return this.customEffects == null || this.customEffects.isEmpty() ? original : this.customEffects;
    }
}
