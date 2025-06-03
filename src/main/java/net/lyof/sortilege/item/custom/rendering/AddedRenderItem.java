package net.lyof.sortilege.item.custom.rendering;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public interface AddedRenderItem {
    float PX_UNIT = 1/16f;

    boolean shouldRender(ItemStack stack);
    void render(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
}
