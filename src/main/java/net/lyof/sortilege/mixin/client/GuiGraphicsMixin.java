package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.item.custom.StaffItem;
import net.lyof.sortilege.item.custom.potion.PotionCooldownManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow public abstract void fill(RenderType layer, int x1, int y1, int x2, int y2, int color);

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.BEFORE))
    public void drawOverchargeBar(Font textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (stack.getItem() instanceof StaffItem staff && staff.getOvercharge(stack) > 0) {
            int i = staff.getOverchargeBarStep(stack);
            int j = staff.getOverchargeBarColor(stack);
            this.fill(RenderType.guiOverlay(), x + 2, y + 14, x + 15, y + 15, -16777216);
            this.fill(RenderType.guiOverlay(), x + 2, y + 14, x + 2 + i, y + 15, j | -16777216);
        }
    }

    @WrapOperation(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;getCooldownPercent(Lnet/minecraft/world/item/Item;F)F"))
    public float getStackCooldown(ItemCooldowns instance, Item item, float tickDelta, Operation<Float> original,
                                  Font textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride) {

        if (item instanceof PotionItem && !(item instanceof AntidotePotionItem) && this.minecraft.player != null) {
            float progress = PotionCooldownManager.getProgress(stack, this.minecraft.player, tickDelta);
            if (progress > 0)
                return progress;
        }
        return original.call(instance, item, tickDelta);
    }
}
