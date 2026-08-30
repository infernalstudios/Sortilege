package net.lyof.sortilege.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.item.potion.PotionCooldownManager;
import net.lyof.sortilege.item.staff.OverchargeEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract int drawString(Font font, Component text, int x, int y, int color, boolean shadow);

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

    @Inject(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = {@At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
                    shift = At.Shift.BEFORE
            )}
    )
    public void renderOverchargeIndicator(Font textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (stack.getItem() instanceof AStaffItem staff && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> handled) {
            ItemStack cursor = handled.getMenu().getCarried();
            if (cursor.isEmpty()) return;

            OverchargeEntry overcharge = staff.getEntry().getCost().getOvercharge();
            if (staff.getOvercharge(stack) >= overcharge.getMax()) return;

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(cursor.getItem());
            if (overcharge.getIngredients().containsKey(id)) {
                this.drawString(textRenderer, Component.literal("+").withStyle(ChatFormatting.YELLOW),
                        x + 19 - 2 - textRenderer.width("+"), y + 6 + 3, 16777215, true);
            }
        }
    }
}
