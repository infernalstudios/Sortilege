package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageEnchantment.class)
public class DamageEnchantmentMixin {
    @Shadow @Final public int type;

    @Inject(method = "doPostAttack", at = @At("HEAD"))
    public void betterBaneOfArthropods(LivingEntity user, Entity target, int level, CallbackInfo ci) {
        if (this.type == 2 && ConfigEntries.betterBane && target instanceof LivingEntity living)
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15 + 15 * level, 1));
    }
}
