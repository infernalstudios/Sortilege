package net.lyof.sortilege.mixin.client;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.rendering.AddedRenderItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
abstract class BuiltinModelItemRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void addItemRender(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo info) {
        if (stack == null) return;

        if (stack.getItem() instanceof AddedRenderItem added && added.shouldRender(stack)) {
            matrices.push();
            added.render(stack, matrices, vertexConsumers, light);
            matrices.pop();
        }
    }
}