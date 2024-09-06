package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow private int scaledHeight;

    @Shadow @Final private static Identifier ICONS;

    @Shadow private int scaledWidth;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        if (ConfigEntries.xpLevelCap > -1) {
            InGameHud self = (InGameHud) (Object) this;

            this.client.getProfiler().push("expBar");
            int i = this.client.player.getNextLevelExperience();
            int k;
            int l;
            if (i > 0) {
                boolean j = true;
                k = (int) (this.client.player.experienceProgress * 183.0F);
                if (this.client.player.experienceLevel >= ConfigEntries.xpLevelCap)
                    k = 183;
                l = this.scaledHeight - 32 + 3;
                context.drawTexture(ICONS, x, l, 0, 64, 182, 5);
                if (k > 0) {
                    context.drawTexture(ICONS, x, l, 0, 69, k, 5);
                }
            }

            this.client.getProfiler().pop();
            if (this.client.player.experienceLevel > 0) {
                this.client.getProfiler().push("expLevel");

                String s = "" + this.client.player.experienceLevel;
                if (this.client.player.experienceLevel >= ConfigEntries.xpLevelCap)
                    s = "MAX";

                k = (this.scaledWidth - this.getTextRenderer().getWidth(s)) / 2;
                l = this.scaledHeight - 31 - 4;
                context.drawText(this.getTextRenderer(), s, k + 1, l, 0, false);
                context.drawText(this.getTextRenderer(), s, k - 1, l, 0, false);
                context.drawText(this.getTextRenderer(), s, k, l + 1, 0, false);
                context.drawText(this.getTextRenderer(), s, k, l - 1, 0, false);
                context.drawText(this.getTextRenderer(), s, k, l, 8453920, false);
                this.client.getProfiler().pop();
            }
            
            ci.cancel();
        }
    }
}
