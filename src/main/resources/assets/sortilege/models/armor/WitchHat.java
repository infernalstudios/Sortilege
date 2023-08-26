// Made with Blockbench 4.6.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class WitchHat<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "witchhat"), "main");
	private final ModelPart hat;

	public WitchHat(ModelPart root) {
		this.hat = root.getChild("hat");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -9.8F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition hat2 = hat.addOrReplaceChild("hat2", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -5.25F, -5.0F, 7.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -8.0F, 2.0F, -0.0524F, 0.0F, 0.0262F));

		PartDefinition hat3 = hat2.addOrReplaceChild("hat3", CubeListBuilder.create().texOffs(0, 23).addBox(-3.25F, -5.25F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.1047F, 0.0F, 0.0524F));

		PartDefinition hat4 = hat3.addOrReplaceChild("hat4", CubeListBuilder.create().texOffs(0, 31).addBox(-1.5F, -3.75F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.2094F, 0.0F, 0.1047F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		hat.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}