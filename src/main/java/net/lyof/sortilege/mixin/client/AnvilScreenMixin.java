package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {
    @ModifyExpressionValue(method = "renderLabels", at = @At(value = "CONSTANT", args = "intValue=40"))
    private int notTooExpensive(int i) {
        return Integer.MAX_VALUE;
    }
}
