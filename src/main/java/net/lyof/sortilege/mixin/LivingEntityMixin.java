package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow @Nullable protected PlayerEntity attackingPlayer;

    @Redirect(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"))
    public void xpDropBonus(ServerWorld world, Vec3d pos, int amount) {
        if (this.attackingPlayer != null && this.attackingPlayer.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.WITCH_HAT)) {
            amount += ConfigEntries.witchHatBonus;
            Sortilege.log("bonus!");
        }

        ExperienceOrbEntity.spawn(world, pos, amount);
    }
}
