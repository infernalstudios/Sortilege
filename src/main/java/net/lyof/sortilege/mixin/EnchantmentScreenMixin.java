package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.mixin.accessor.ScreenAccessor;
import net.lyof.sortilege.util.IMixinAccess;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler> {
    public EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    private static final Identifier CATALYST_TEXTURE = Sortilege.makeID("textures/gui/catalyst_overlay.png");

    @Inject(method = "drawBackground", at = @At("TAIL"))
    public void drawCatalystEffect(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        for (int k = 0; k < 3; k++) {
            if (((IMixinAccess) this.handler).getProperty(k) == 1)
                context.drawTexture(CATALYST_TEXTURE, i + 60, j + 14 + 19 * k, 36, 0, 108, 19);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/EnchantmentScreen;drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V", shift = At.Shift.BEFORE))
    public void drawCatalystSlot(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (((IMixinAccess) this.handler).getProperty(3) == 0) return;

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        int frame = ((ScreenAccessor) this).getClient().player.age / 3 % 18;
        int x = frame >= 9 ? 18 : 0;
        int y = (frame % 9) * 18;

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 200);
        context.drawTexture(CATALYST_TEXTURE, i + 24, j + 19, x, y, 18, 18);
        context.getMatrices().pop();
    }
/*
    @Inject(method = "drawBook", at = @At("HEAD"), cancellable = true)
    public void cancelBook(DrawContext context, int x, int y, float delta, CallbackInfo ci) {
        ci.cancel();
    }*/
}
