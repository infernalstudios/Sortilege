package net.lyof.sortilege.mixins;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {
    @Inject(method = "restoreFrom", at = @At(value = "RETURN"))
    public void keepInventory(ServerPlayer player, boolean force, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        self.getInventory().replaceWith(player.getInventory());
    }
}
