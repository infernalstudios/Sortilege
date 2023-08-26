package net.lyof.sortilege.items.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.items.armor.ModArmorMaterials;
import net.lyof.sortilege.items.armor.rendering.WitchHatModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class WitchHatItem extends ArmorItem {
    public WitchHatItem(Properties builder) {
        super(ModArmorMaterials.WITCH, EquipmentSlot.HEAD, builder);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
                HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(), Map.of("head",
                        new WitchHatModel(Minecraft.getInstance().getEntityModels().bakeLayer(WitchHatModel.LAYER_LOCATION)).hat, "hat",
                        new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body",
                        new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm",
                        new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm",
                        new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg",
                        new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg",
                        new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                armorModel.crouching = living.isShiftKeyDown();
                armorModel.riding = defaultModel.riding;
                armorModel.young = living.isBaby();
                return armorModel;
            }
        });
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return Sortilege.MOD_ID + ":textures/models/armor/witch_hat.png";
    }
}
