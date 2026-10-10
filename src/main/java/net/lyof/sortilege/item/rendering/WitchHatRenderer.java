package net.lyof.sortilege.item.rendering;

public class WitchHatRenderer {/*TODOimplements ArmorRenderer {
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
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, model.renderType(TEXTURE), stack.hasFoil());
            model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
            matrices.popPose();
        }
    }*/
}
