package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {
    @ModifyExpressionValue(method = "drawForeground", at = @At(value = "CONSTANT", args = "intValue=40"))
    private int notTooExpensive(int i) {
        return Integer.MAX_VALUE;
    }
}
