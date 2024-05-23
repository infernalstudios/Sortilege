package net.lyof.sortilege.mixins;

import net.lyof.sortilege.configs.ConfigEntries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public class MixinAnvilMenu {
    @Shadow @Final private DataSlot cost;

    @Inject(method = "createResult", at = @At("RETURN"))
    public void noAnvilCost(CallbackInfo ci) {
        if (ConfigEntries.noXPAnvil) this.cost.set(0);
    }

    @Inject(method = "mayPickup", at = @At("RETURN"), cancellable = true)
    public void canTakeFix(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
