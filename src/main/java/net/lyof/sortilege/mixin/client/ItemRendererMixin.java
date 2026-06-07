package net.lyof.sortilege.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lyof.sortilege.item.custom.rendering.AddedRenderItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V",
            shift = At.Shift.AFTER)
    )
    public void addItemRender(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices,
                           MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (stack == null) return;

        if (stack.getItem() instanceof AddedRenderItem added && added.shouldRender(stack)) {
            matrices.pushPose();
            added.render(stack, matrices, vertexConsumers, light);
            matrices.popPose();
        }
    }
}