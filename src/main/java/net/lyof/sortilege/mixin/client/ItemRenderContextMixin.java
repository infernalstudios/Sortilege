package net.lyof.sortilege.mixin.client;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext;
import net.lyof.sortilege.item.custom.rendering.AddedRenderItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderContext.class)
public class ItemRenderContextMixin {
    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BakedModel;emitItemQuads(Lnet/minecraft/item/ItemStack;Ljava/util/function/Supplier;Lnet/fabricmc/fabric/api/renderer/v1/render/RenderContext;)V"))
    private void addItemRender(ItemStack stack, ModelTransformationMode transformMode, boolean invert, MatrixStack matrices,
                               VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (stack == null) return;

        if (stack.getItem() instanceof AddedRenderItem added && added.shouldRender(stack)) {
            matrices.push();
            added.render(stack, matrices, vertexConsumers, light);
            matrices.pop();
        }
    }
}
