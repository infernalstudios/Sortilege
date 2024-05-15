package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.utils.XPHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

        if (this.getDataTracker().containsKey(XPHelper.BOUNTY)) {
            Sortilege.log(" + " + this.getDataTracker().get(XPHelper.BOUNTY));
            amount += this.getDataTracker().get(XPHelper.BOUNTY);
        }

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

    @Inject(method = "initDataTracker", at = @At("HEAD"))
    public void trackXPBounty(CallbackInfo ci) {
        this.getDataTracker().startTracking(XPHelper.BOUNTY, 0);
    }


    @Inject(method = "drop", at = @At("HEAD"))
    public void giveKillXP(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof PlayerEntity player && this.getWorld() instanceof ServerWorld world) {
            int steal_xp = (int) Math.round(XPHelper.getTotalxp(player.experienceLevel, player.experienceProgress, world) * ConfigEntries.attackerXPRatio);
            Entity source = damageSource.getAttacker();

            if (source instanceof LivingEntity attacker) {
                attacker.getDataTracker().set(XPHelper.BOUNTY, steal_xp);
                attacker.setGlowing(true);
            }
            else ExperienceOrbEntity.spawn(world, this.getPos(), steal_xp);
        }
    }

    @Inject(method = "shouldDropXp", at = @At("HEAD"), cancellable = true)
    public void alwaysDropBounty(CallbackInfoReturnable<Boolean> cir) {
        if (this.getDataTracker().containsKey(XPHelper.BOUNTY) && this.getDataTracker().get(XPHelper.BOUNTY) > 0)
            cir.setReturnValue(true);
    }
}
