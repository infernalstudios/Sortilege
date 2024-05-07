package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow @Nullable protected PlayerEntity attackingPlayer;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"))
    public void xpDropBonus(ServerWorld world, Vec3d pos, int amount) {
        if (this.attackingPlayer != null && this.attackingPlayer.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.WITCH_HAT))
            amount += ConfigEntries.witchHatBonus;

        ExperienceOrbEntity.spawn(world, pos, amount);
    }

    @Inject(method = "dropLoot", at = @At("HEAD"))
    public void witchHatDrop(DamageSource damageSource, boolean causedByPlayer, CallbackInfo ci) {
        if (causedByPlayer && this.getType() == EntityType.WITCH) {
            World world = this.getWorld();
            if (world.isClient()) return;

            if (Math.random() > ConfigEntries.witchHatDropChance) return;

            ItemStack hat = ModItems.WITCH_HAT.getDefaultStack();
            hat.setDamage((int) Math.round(Math.random() * (hat.getMaxDamage() - 10)) + 10);
            world.spawnEntity(new ItemEntity(world, this.getX(), this.getY(), this.getZ(), hat));
        }
    }
}
