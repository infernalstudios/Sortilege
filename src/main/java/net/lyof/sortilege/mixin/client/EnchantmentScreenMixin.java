package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.util.inject.EnchantInfoHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {
    public EnchantmentScreenMixin(EnchantmentMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Unique
    private static final ResourceLocation CATALYST_TEXTURE = Sortilege.makeID("textures/gui/catalyst_overlay.png");

    @Inject(method = "renderBg", at = @At("TAIL"))
    public void drawCatalystEffect(GuiGraphics context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        for (int k = 0; k < 3; k++) {
            if (((EnchantInfoHolder) this.menu).sorti_isCatalyzed(k))
                context.blit(CATALYST_TEXTURE, i + 60, j + 14 + 19 * k, 36, 0, 108, 19);

            else if (this.menu.costs[k] == 0 && ((EnchantInfoHolder) this.menu).sorti_hasEnchantableItem()
                    && this.isHovering(60, 14 + 19*k, 108, 18, mouseX, mouseY)) {
                context.renderTooltip(this.font, Component.translatable("sortilege.enchanting.requires_catalyst").withStyle(ChatFormatting.RED),
                        mouseX, mouseY);
            }
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EnchantmentScreen;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V", shift = At.Shift.BEFORE))
    public void drawCatalystSlot(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!((EnchantInfoHolder) this.menu).sorti_hasEnchantableItem()) return;

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        int frame = this.minecraft.player.tickCount / 2 % 18;
        int x = frame >= 9 ? 18 : 0;
        int y = (frame % 9) * 18;

        context.blit(CATALYST_TEXTURE, i + 24, j + 19, 200, x, y, 18, 18, 256, 256);
    }

    @WrapOperation(method = "tickBook", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    public float forceRenderBook(float value, float min, float max, Operation<Float> original) {
        if (((EnchantInfoHolder) this.menu).sorti_hasEnchantableItem() && this.menu.costs[0] == 0
                && this.menu.costs[1] == 0 && this.menu.costs[2] == 0)
            value += 0.4f;

        return original.call(value, min, max);
    }
}
