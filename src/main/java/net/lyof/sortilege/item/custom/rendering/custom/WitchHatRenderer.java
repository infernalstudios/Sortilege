package net.lyof.sortilege.item.custom.rendering.custom;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class WitchHatRenderer implements ArmorRenderer {
    private static WitchHatModel<?> model = null;

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity,
                       EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {

        if (!stack.isEmpty() && stack.isOf(ModItems.WITCH_HAT)) {
            if (model == null) model = new WitchHatModel<>(WitchHatModel.getTexturedModelData().createModel());

            matrices.push();
            contextModel.getHead().rotate(matrices);
            matrices.translate(0.0D, -1.75D, 0.0D);
            matrices.scale(1.19F, 1.19F, 1.19F);
            VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, model.getLayer(Sortilege.makeID("textures/models/armor/witch_hat.png")), false, stack.hasGlint());
            model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.pop();
        }
    }
}
