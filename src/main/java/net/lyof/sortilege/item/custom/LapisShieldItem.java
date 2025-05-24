package net.lyof.sortilege.item.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

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

    private static final String COOLDOWN_NBT = Sortilege.MOD_ID + "_LastUse";

    public static void putOnCooldown(ItemStack stack, PlayerEntity player) {
        stack.getOrCreateNbt().putInt(COOLDOWN_NBT, (int) player.getWorld().getTime());
    }

    public static int getCooldownEnd(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(COOLDOWN_NBT) + ConfigEntries.lapisShieldCooldown;
    }

    public static void removeCooldown(ItemStack stack) {
        stack.getOrCreateNbt().remove(COOLDOWN_NBT);
    }

    public static boolean isOnCooldown(ItemStack stack) {
        return stack.getOrCreateNbt().contains(COOLDOWN_NBT);
    }
}
