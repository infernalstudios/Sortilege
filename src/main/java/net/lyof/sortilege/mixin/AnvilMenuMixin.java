package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @Shadow @Final private DataSlot cost;

    @WrapMethod(method = "createResult")
    public void noAnvilCost(Operation<Void> original) {
        original.call();
        if (ModConfig.noXPAnvil.get()) this.cost.set(0);
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void canTakeFix(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.noXPAnvil.get()) cir.setReturnValue(true);
    }

    @ModifyExpressionValue(method = "createResult", at = @At(value = "CONSTANT", args = "intValue=40"))
    private int notTooExpensive40(int i) {
        return Integer.MAX_VALUE;
    }

    @ModifyExpressionValue(method = "createResult", at = @At(value = "CONSTANT", args = "intValue=39"))
    private int notTooExpensive39(int i) {
        return Integer.MAX_VALUE - 1;
    }
}
