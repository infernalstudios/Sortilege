package net.lyof.sortilege.item.custom.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

public interface AddedRenderItem {
    float PX_UNIT = 1/16f;

    boolean shouldRender(ItemStack stack);
    void render(ItemStack stack, PoseStack matrices, MultiBufferSource vertexConsumers, int light);
}
