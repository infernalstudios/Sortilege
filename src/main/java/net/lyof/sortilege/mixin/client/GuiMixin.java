package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private int screenHeight;
    @Shadow @Final private static ResourceLocation GUI_ICONS_LOCATION;
    @Shadow public abstract Font getFont();

    @WrapOperation(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    public int renderExperienceBar(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean dropShadow, Operation<Integer> original) {
        if (ConfigEntries.xpLevelCap > -1 && this.minecraft.player.experienceLevel >= ConfigEntries.xpLevelCap)
            text = "MAX";
        return original.call(instance, font, text, x, y, color, dropShadow);
    }
}
