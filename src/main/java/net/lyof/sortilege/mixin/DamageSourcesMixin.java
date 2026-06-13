package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.staff.StaffItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DamageSources.class)
public abstract class DamageSourcesMixin {
    @Shadow public abstract DamageSource indirectMagic(Entity source, @Nullable Entity attacker);

    @ModifyReturnValue(method = "playerAttack", at = @At("RETURN"))
    public DamageSource arcaneDamage(DamageSource original, Player attacker) {
        if ((ModEnchants.ARCANE != null && EnchantmentHelper.getEnchantmentLevel(ModEnchants.ARCANE, attacker) > 0)
                || attacker.getMainHandItem().getItem() instanceof StaffItem) {
            return this.indirectMagic(attacker, attacker);
        }
        return original;
    }
}
