package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity {
    @Shadow private PlayerEntity target;

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "expensiveUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getClosestPlayer(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/PlayerEntity;"))
    public PlayerEntity ignoreCappedPlayers(World instance, Entity entity, double v, Operation<PlayerEntity> original) {
        if (ConfigEntries.xpLevelCap > -1 && instance.getPlayers().size() > 1)
            return instance.getClosestPlayer(entity.getX(), entity.getY(), entity.getZ(), v,
                    e -> e instanceof PlayerEntity p && !p.isSpectator() && !p.isCreative() && p.experienceLevel < ConfigEntries.xpLevelCap);
        return original.call(instance, entity, v);
    }

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    public void cancelCappedPlayerCollision(PlayerEntity player, CallbackInfo ci) {
        if (ConfigEntries.xpLevelCap > -1 && player.experienceLevel >= ConfigEntries.xpLevelCap && this.getWorld().getPlayers().size() > 1
                && this.target != null)
            ci.cancel();
    }
}
