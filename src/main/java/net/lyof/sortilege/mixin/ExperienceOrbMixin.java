package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin extends Entity {
    @Shadow private Player followingPlayer;

    public ExperienceOrbMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @WrapOperation(method = "scanForEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getNearestPlayer(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/entity/player/Player;"))
    public Player ignoreCappedPlayers(Level instance, Entity entity, double v, Operation<Player> original) {
        if (ConfigEntries.xpLevelCap > -1 && instance.players().size() > 1)
            return instance.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), v,
                    e -> e instanceof Player p && !p.isSpectator() && !p.isCreative() && p.experienceLevel < ConfigEntries.xpLevelCap);
        return original.call(instance, entity, v);
    }

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    public void cancelCappedPlayerCollision(Player player, CallbackInfo ci) {
        if (ConfigEntries.xpLevelCap > -1 && player.experienceLevel >= ConfigEntries.xpLevelCap && this.level().players().size() > 1
                && this.followingPlayer != null)
            ci.cancel();
    }
}
