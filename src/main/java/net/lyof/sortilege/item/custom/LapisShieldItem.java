package net.lyof.sortilege.item.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.particle.ModParticles;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

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

    public static void putOnCooldown(ItemStack stack, LivingEntity player) {
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

    public static void onSuccessfulUse(ItemStack stack, LivingEntity entity, float amount) {
        LapisShieldItem.putOnCooldown(stack, entity);
        ModParticles.spawnWisps(entity.getWorld(), entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()) / 2, entity.getZ(),
                16, new float[]{0.3f, 0.3f, 1f});

        if (amount >= 3f) {
            stack.damage(1, entity, e -> e.sendToolBreakStatus(Hand.OFF_HAND));
            if (stack.isEmpty())
                entity.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + entity.getWorld().random.nextFloat() * 0.4F);
        }
    }
}
