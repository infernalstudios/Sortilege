package net.lyof.sortilege.item.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class WitchHatRenderer implements IClientItemExtensions {
    private static WitchHatModel model = null;
    public static final ResourceLocation TEXTURE = Sortilege.MOD.makeID("textures/models/armor/witch_hat.png");

    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack stack, LivingEntity entity,
                       EquipmentSlot slot, int light, HumanoidModel<LivingEntity> contextModel) {

        if (!stack.isEmpty() && stack.is(ModItems.WITCH_HAT.get())) {
            if (model == null) model = new WitchHatModel(WitchHatModel.getTexturedModelData().bakeRoot());

            matrices.pushPose();
            contextModel.getHead().translateAndRotate(matrices);
            matrices.translate(0.0D, -1.75D, 0.0D);
            matrices.scale(1.19F, 1.19F, 1.19F);
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, model.renderType(TEXTURE), stack.hasFoil());
            model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
            matrices.popPose();
        }
    }

    @Override
    public Model getGenericArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        if (model == null) model = new WitchHatModel(WitchHatModel.getTexturedModelData().bakeRoot());
        return model;
    }

    @Override
    public void setupModelAnimations(LivingEntity entity, ItemStack stack, EquipmentSlot equipmentSlot, Model model,
                                     float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                                     float netHeadYaw, float headPitch) {
        if (model instanceof WitchHatModel m)
            m.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
