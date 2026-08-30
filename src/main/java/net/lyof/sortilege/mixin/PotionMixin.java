package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lyof.sortilege.item.potion.CustomPotionData;
import net.lyof.sortilege.item.potion.PotionShenanigans;
import net.lyof.sortilege.mixin.accessor.MobEffectInstanceAccessor;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Potion.class)
public class PotionMixin implements PotionShenanigans {
    @Unique private List<MobEffectInstance> sorti_customEffects = null;
    @Unique private boolean sorti_lengthened = false;

    @Override public void sorti_setImmunity(Holder<MobEffect> effect, int time) {}

    @Override
    public void sorti_resetPotionCache() {
        this.sorti_customEffects = null;
    }

    @ModifyReturnValue(method = "getEffects", at = @At("RETURN"))
    public List<MobEffectInstance> lengthenEffects(List<MobEffectInstance> original) {
        if (this.sorti_customEffects == null) {
            if (!CustomPotionData.isEmpty()) {
                CustomPotionData data = CustomPotionData.get(BuiltInRegistries.POTION.wrapAsHolder((Potion) (Object) this));
                if (data == null)
                    this.sorti_customEffects = List.of();
                else
                    this.sorti_customEffects = data.effects;
            }
        }

        if (!this.sorti_lengthened) {
            for (MobEffectInstance effect : original) {
                ((MobEffectInstanceAccessor) effect).setDuration((int) Math.round(effect.getDuration() * ModConfig.potionDurationMultiplier.get()));
            }
            this.sorti_lengthened = true;
        }

        return this.sorti_customEffects == null || this.sorti_customEffects.isEmpty() ? original : this.sorti_customEffects;
    }
}
