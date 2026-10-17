package net.lyof.sortilege.item.rendering;

import net.lyof.sortilege.Sortilege;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class WitchHatRenderer implements IClientItemExtensions {
    private static WitchHatModel model = null;
    public static final ResourceLocation TEXTURE = Sortilege.MOD.makeID("textures/models/armor/witch_hat.png");

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
