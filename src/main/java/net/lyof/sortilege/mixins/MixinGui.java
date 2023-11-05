package net.lyof.sortilege.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lyof.sortilege.configs.ConfigEntries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {
    @Shadow protected int screenHeight;
    @Shadow protected int screenWidth;
    @Final @Shadow protected Minecraft minecraft;

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBar(PoseStack poseStack, int n, CallbackInfo ci) {
        if (ConfigEntries.xpLevelCap > -1) {
            Gui self = (Gui) (Object) this;
            Minecraft minecraft = this.minecraft;
            
            minecraft.getProfiler().push("expBar");
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            int i = minecraft.player.getXpNeededForNextLevel();
            if (i > 0) {
                int j = 182;
                int k = (int)(minecraft.player.experienceProgress * 183.0F);
                if (minecraft.player.experienceLevel >= ConfigEntries.xpLevelCap)
                    k = j;
                int l = this.screenHeight - 32 + 3;
                self.blit(poseStack, n, l, 0, 64, j, 5);
                if (k > 0) {
                    self.blit(poseStack, n, l, 0, 69, k, 5);
                }
            }

            minecraft.getProfiler().pop();
            if (minecraft.player.experienceLevel > 0) {
                minecraft.getProfiler().push("expLevel");

                String s = "" + minecraft.player.experienceLevel;
                if (minecraft.player.experienceLevel >= ConfigEntries.xpLevelCap)
                    s = "MAX";

                int i1 = (this.screenWidth - self.getFont().width(s)) / 2;
                int j1 = this.screenHeight - 31 - 4;
                self.getFont().draw(poseStack, s, (float)(i1 + 1), (float)j1, 0);
                self.getFont().draw(poseStack, s, (float)(i1 - 1), (float)j1, 0);
                self.getFont().draw(poseStack, s, (float)i1, (float)(j1 + 1), 0);
                self.getFont().draw(poseStack, s, (float)i1, (float)(j1 - 1), 0);
                self.getFont().draw(poseStack, s, (float)i1, (float)j1, 8453920);
                minecraft.getProfiler().pop();
            }
            
            ci.cancel();
        }
    }
}
