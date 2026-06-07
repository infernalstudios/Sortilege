package net.lyof.sortilege.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lyof.sortilege.item.custom.rendering.AddedRenderItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityWithoutLevelRenderer.class)
abstract class BlockEntityWithoutLevelRendererMixin {
    @Inject(method = "renderByItem", at = @At("TAIL"))
    private void addItemRender(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, CallbackInfo info) {
        if (stack == null) return;

        if (stack.getItem() instanceof AddedRenderItem added && added.shouldRender(stack)) {
            matrices.pushPose();
            added.render(stack, matrices, vertexConsumers, light);
            matrices.popPose();
        }
    }
}