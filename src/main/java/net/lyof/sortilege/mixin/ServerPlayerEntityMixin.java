package net.lyof.sortilege.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Redirect(method = "copyFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    public boolean keepInventory(ServerPlayerEntity instance) {
        return true;
    }
}
