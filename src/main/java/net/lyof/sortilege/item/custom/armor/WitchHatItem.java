package net.lyof.sortilege.item.custom.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.armor.rendering.WitchHatModel;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class WitchHatItem extends ArmorItem {
    private static final UUID[] MODIFIERS = new UUID[] { UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150") };
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
    private final int protection;
    private final float toughness;
    protected final float knockbackResistance;
    protected final EquipmentSlot slot;
    protected final ArmorMaterial material;

    public WitchHatItem(Settings settings) {
        super(ModArmorMaterials.WITCH, Type.HELMET, settings);
        this.material = ModArmorMaterials.WITCH;
        this.slot = Type.HELMET.getEquipmentSlot();
        this.protection = material.getProtection(type);
        this.toughness = material.getToughness();
        this.knockbackResistance = material.getKnockbackResistance();
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        UUID uUID = MODIFIERS[slot.getEntitySlotId()];
        builder.put(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(uUID, "Armor modifier", (double) this.protection, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, new EntityAttributeModifier(uUID, "Armor toughness", (double) this.toughness, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, new EntityAttributeModifier(uUID, "Armor knockback resistance", (double) this.knockbackResistance / 10D, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == this.slot ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }


    public static class Renderer implements ArmorRenderer {
        private static final WitchHatModel<?> model = new WitchHatModel<>(WitchHatModel.getTexturedModelData().createModel());


        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity,
                           EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {

            if (!stack.isEmpty() && stack.isOf(ModItems.WITCH_HAT)) {
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
}
