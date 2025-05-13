package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.StaffItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DamageSources.class)
public abstract class DamageSourcesMixin {
    @Shadow public abstract DamageSource indirectMagic(Entity source, @Nullable Entity attacker);

    @ModifyReturnValue(method = "playerAttack", at = @At("RETURN"))
    public DamageSource arcaneDamage(DamageSource original, PlayerEntity attacker) {
        if ((ModEnchants.ARCANE != null && EnchantmentHelper.getEquipmentLevel(ModEnchants.ARCANE, attacker) > 0)
                || attacker.getMainHandStack().getItem() instanceof StaffItem) {
            return this.indirectMagic(attacker, attacker);
        }
        return original;
    }
}
