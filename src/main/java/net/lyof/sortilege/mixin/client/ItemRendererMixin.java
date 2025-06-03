package net.lyof.sortilege.mixin.client;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.MockItemRenderer;
import net.lyof.sortilege.item.custom.LapisShieldItem;
import net.lyof.sortilege.item.custom.StaffItem;
import net.lyof.sortilege.setup.ModTags;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Unique private static final float px = 1/16f;

    @Unique
    private static final Identifier GLINT_TEXTURE = Sortilege.makeID("textures/models/staff/glint.png");
    @Unique
    private static final Identifier SHIELD_GLOW_LAYER = Sortilege.makeID("textures/models/lapis_shield_glow_layer.png");

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V",
            shift = At.Shift.AFTER)
    )
    public void renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        MinecraftClient minecraft = MinecraftClient.getInstance();

        if (minecraft.world == null || stack == null) return;

        if (stack.getItem() instanceof StaffItem staff && staff.hasColor(stack) && !stack.isIn(ModTags.Items.NO_DYE_OVERLAY_STAFFS)) {
            matrices.push();

            matrices.scale(1.005f, 1.005f, 1.005f);
            matrices.translate(0, 0.995, 0.4975);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

            MockItemRenderer.renderTintedItem(matrices, vertexConsumers, light,
                    GLINT_TEXTURE, staff.getColor(stack));

            matrices.pop();
        }

        if (stack.getItem() instanceof LapisShieldItem && !LapisShieldItem.isOnCooldown(stack)) {
            matrices.push();

            matrices.scale(1.005f, 1.005f, 1.005f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            matrices.translate(-6*px, -4*px, -1.5*px);

            MockItemRenderer.renderItem(matrices, vertexConsumers, 15728880, SHIELD_GLOW_LAYER);

            matrices.pop();
        }
    }
}