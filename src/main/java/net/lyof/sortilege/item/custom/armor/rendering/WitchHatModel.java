package net.lyof.sortilege.item.custom.armor.rendering;

import net.lyof.sortilege.Sortilege;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class WitchHatModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Sortilege.makeID("witch_hat"), "main");
	public final ModelPart hat;

	public WitchHatModel(ModelPart root) {
		this.hat = root.getChild("hat");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData meshdefinition = new ModelData();
		ModelPartData partdefinition = meshdefinition.getRoot();

		ModelPartData hat = partdefinition.addChild("hat", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -7.8F, -5.0F, 10.0F, 2.0F, 10.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		ModelPartData hat2 = hat.addChild("hat2", ModelPartBuilder.create().uv(0, 12).cuboid(-5.0F, -3.25F, -5.0F, 7.0F, 4.0F, 7.0F, new Dilation(0.0F)), ModelTransform.of(1.75F, -8.0F, 2.0F, -0.0524F, 0.0F, 0.0262F));
		ModelPartData hat3 = hat2.addChild("hat3", ModelPartBuilder.create().uv(0, 23).cuboid(-3.25F, -3.25F, -3.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -3.0F, 0.0F, -0.1047F, 0.0F, 0.0524F));
		ModelPartData hat4 = hat3.addChild("hat4", ModelPartBuilder.create().uv(0, 31).cuboid(-1.5F, -1.75F, -1.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.25F)), ModelTransform.of(0.0F, -3.0F, 0.0F, -0.2094F, 0.0F, 0.1047F));

		return TexturedModelData.of(meshdefinition, 64, 64);
	}

	@Override
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.hat.yaw = netHeadYaw / (180F / (float) Math.PI);
		this.hat.pitch = headPitch / (180F / (float) Math.PI);
	}

	@Override
	public void render(MatrixStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		hat.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}