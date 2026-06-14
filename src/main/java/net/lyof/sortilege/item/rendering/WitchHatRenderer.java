package net.lyof.sortilege.item.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class WitchHatRenderer implements ArmorRenderer {
    private static WitchHatModel<?> model = null;
    private static final ResourceLocation TEXTURE = Sortilege.MOD.makeID("textures/models/armor/witch_hat.png");

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack stack, LivingEntity entity,
                       EquipmentSlot slot, int light, HumanoidModel<LivingEntity> contextModel) {

        if (!stack.isEmpty() && stack.is(ModItems.WITCH_HAT)) {
            if (model == null) model = new WitchHatModel<>(WitchHatModel.getTexturedModelData().bakeRoot());

            matrices.pushPose();
            contextModel.getHead().translateAndRotate(matrices);
            matrices.translate(0.0D, -1.75D, 0.0D);
            matrices.scale(1.19F, 1.19F, 1.19F);
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, model.renderType(TEXTURE), false, stack.hasFoil());
            model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.popPose();
        }
    }
}
