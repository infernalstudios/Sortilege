package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageEnchantment.class)
public class DamageEnchantmentMixin {
    @Shadow @Final public int typeIndex;

    @Inject(method = "onTargetDamaged", at = @At("HEAD"))
    public void betterBaneOfArthropods(LivingEntity user, Entity target, int level, CallbackInfo ci) {
        if (this.typeIndex == 2 && ConfigEntries.betterBane && target instanceof LivingEntity living)
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 + user.getRandom().nextInt(10 * level), 1));
    }
}
