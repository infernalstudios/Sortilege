package net.lyof.sortilege.item.custom;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;

public class LapisShieldItem extends Item implements Equipment {
    public LapisShieldItem(Settings settings) {
        super(settings);
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.OFFHAND;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(Items.LAPIS_LAZULI) || super.canRepair(stack, ingredient);
    }

    public static void putOnCooldown(ItemStack stack, PlayerEntity player) {
        player.getItemCooldownManager().set(stack.getItem(), 80);
    }
}
