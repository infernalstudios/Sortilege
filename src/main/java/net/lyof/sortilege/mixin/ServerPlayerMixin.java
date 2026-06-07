package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @WrapOperation(method = "restoreFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"))
    public boolean keepInventory(ServerPlayer instance, Operation<Boolean> original) {
        return true;
    }
}
