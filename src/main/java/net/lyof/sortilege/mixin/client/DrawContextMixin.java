package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.item.custom.StaffItem;
import net.lyof.sortilege.item.custom.potion.PotionCooldownManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow public abstract void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int color);

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.BEFORE))
    public void drawOverchargeBar(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (stack.getItem() instanceof StaffItem staff && staff.getOvercharge(stack) > 0) {
            int i = staff.getOverchargeBarStep(stack);
            int j = staff.getOverchargeBarColor(stack);
            this.fill(RenderLayer.getGuiOverlay(), x + 2, y + 14, x + 15, y + 15, -16777216);
            this.fill(RenderLayer.getGuiOverlay(), x + 2, y + 14, x + 2 + i, y + 15, j | -16777216);
        }
    }

    @WrapOperation(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;getCooldownProgress(Lnet/minecraft/item/Item;F)F"))
    public float getStackCooldown(ItemCooldownManager instance, Item item, float tickDelta, Operation<Float> original,
                                  TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride) {

        if (item instanceof PotionItem && !(item instanceof AntidotePotionItem) && this.client.player != null) {
            float progress = PotionCooldownManager.getProgress(stack, this.client.player, tickDelta);
            if (progress > 0)
                return progress;
        }
        return original.call(instance, item, tickDelta);
    }
}
