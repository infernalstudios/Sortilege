package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ModConfig;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
	@Inject(at = @At("HEAD"), method = "onPlayerConnected")
	private void init(CallbackInfo info) {
		ModConfig.register();
	}
}