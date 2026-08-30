package net.lyof.sortilege.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lcc.sollib.api.client.render.MockItemRenderer;
import net.lcc.sollib.api.client.render.item.IAddedRenderItem;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import java.util.List;

public class LapisShieldItem extends Item implements Equipable, IAddedRenderItem {
    public LapisShieldItem(Properties settings) {
        super(settings.component(ModDataComponents.LAPIS_SHIELD_COOLDOWN, 0));
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.OFFHAND;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return ingredient.is(Items.LAPIS_LAZULI) || super.isValidRepairItem(stack, ingredient);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.modifiers.offhand").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.sortilege.lapis_shield.desc", ModConfig.lapisShieldCooldown.get() / 20f)
                .withStyle(ChatFormatting.GRAY));
    }

    public static void addCooldown(ItemStack stack, int time) {
        stack.set(ModDataComponents.LAPIS_SHIELD_COOLDOWN, time);
    }

    public static void removeCooldown(ItemStack stack) {
        stack.remove(ModDataComponents.LAPIS_SHIELD_COOLDOWN);
    }

    public static int getCooldownEnd(ItemStack stack) {
        return stack.has(ModDataComponents.LAPIS_SHIELD_COOLDOWN)
                ? stack.get(ModDataComponents.LAPIS_SHIELD_COOLDOWN) + ModConfig.lapisShieldCooldown.get()
                : 0;
    }

    public static boolean isOnCooldown(ItemStack stack) {
        return stack.has(ModDataComponents.LAPIS_SHIELD_COOLDOWN);
    }

    public static void onSuccessfulUse(ItemStack stack, LivingEntity entity, float amount) {
        if (!entity.level().isClientSide()) LapisShieldItem.addCooldown(stack, entity.tickCount);
        sendCooldownUpdate(entity, entity.tickCount);

        if (entity instanceof Player player)
            player.getCooldowns().addCooldown(ModItems.LAPIS_SHIELD, ModConfig.lapisShieldCooldown.get());

        ModParticles.sendParticles(entity.level(), entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()) / 2, entity.getZ(),
                16, 0x5555ff);

        if (amount >= 3f) {
            stack.hurtAndBreak(Math.max(1, (int) amount/2), entity, EquipmentSlot.OFFHAND);
            if (stack.isEmpty())
                entity.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + entity.level().random.nextFloat() * 0.4F);
        }
    }

    public static void sendCooldownUpdate(LivingEntity entity, int cooldown) {
        if (!entity.level().isClientSide()) {
            ModPackets.LapisShieldPacket packet = new ModPackets.LapisShieldPacket(entity.getId(), cooldown);

            for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) entity.level(), entity.blockPosition())) {
                ServerPlayNetworking.send(player, packet);
            }
        }
    }


    private static final ResourceLocation SHIELD_GLOW_LAYER = Sortilege.MOD.makeID("textures/models/lapis_shield_glow_layer.png");

    @Override
    public boolean shouldRender(ItemStack stack) {
        return !LapisShieldItem.isOnCooldown(stack);
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        matrices.scale(1.005f, 1.005f, 1.005f);
        matrices.mulPose(Axis.XP.rotationDegrees(180));
        matrices.translate(-6 * PX_UNIT, -4 * PX_UNIT, -2 * PX_UNIT);

        MockItemRenderer.renderItem(matrices, vertexConsumers, -1, SHIELD_GLOW_LAYER);
    }
}
