package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {
    @Shadow @Final private Property levelCost;

    @WrapMethod(method = "updateResult")
    public void noAnvilCost(Operation<Void> original) {
        original.call();
        if (ConfigEntries.noXPAnvil) this.levelCost.set(0);
    }

    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    public void canTakeFix(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (ConfigEntries.noXPAnvil) cir.setReturnValue(true);
    }

    @ModifyExpressionValue(method = "updateResult", at = @At(value = "CONSTANT", args = "intValue=40"))
    private int notTooExpensive40(int i) {
        return Integer.MAX_VALUE;
    }

    @ModifyExpressionValue(method = "updateResult", at = @At(value = "CONSTANT", args = "intValue=39"))
    private int notTooExpensive39(int i) {
        return Integer.MAX_VALUE - 1;
    }
}
