package net.lyof.sortilege.mixin;

import net.lyof.sortilege.enchant.ModEnchants;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSources.class)
public abstract class DamageSourcesMixin {
    @Shadow public abstract DamageSource indirectMagic(Entity source, @Nullable Entity attacker);

    @Inject(method = "playerAttack", at = @At("HEAD"), cancellable = true)
    public void arcaneDamage(PlayerEntity attacker, CallbackInfoReturnable<DamageSource> cir) {
        if (EnchantmentHelper.getEquipmentLevel(ModEnchants.ARCANE, attacker) > 0) {
            cir.setReturnValue(this.indirectMagic(attacker, attacker));
        }
    }
}
