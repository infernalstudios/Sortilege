package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
    @Shadow @Final private Property levelCost;

    @Inject(method = "updateResult", at = @At("RETURN"))
    public void noAnvilCost(CallbackInfo ci) {
        if (ConfigEntries.noXPAnvil) this.levelCost.set(0);
    }

    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    public void canTakeFix(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (ConfigEntries.noXPAnvil) cir.setReturnValue(true);
    }
}
