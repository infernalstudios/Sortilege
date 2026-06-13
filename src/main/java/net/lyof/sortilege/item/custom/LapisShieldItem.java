package net.lyof.sortilege.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lcc.sollib.api.client.render.MockItemRenderer;
import net.lcc.sollib.api.client.render.item.IAddedRenderItem;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LapisShieldItem extends Item implements Equipable, IAddedRenderItem {
    public LapisShieldItem(Properties settings) {
        super(settings);
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
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        tooltip.add(Component.translatable("item.modifiers.offhand").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.sortilege.lapis_shield.desc", ModConfig.lapisShieldCooldown.get() / 20f)
                .withStyle(ChatFormatting.GRAY));
    }

    private static final String COOLDOWN_NBT = Sortilege.MOD_ID + "_LastUse";

    public static void addCooldown(ItemStack stack, int time) {
        stack.getOrCreateTag().putInt(COOLDOWN_NBT, time);
    }

    public static void removeCooldown(ItemStack stack) {
        stack.getOrCreateTag().remove(COOLDOWN_NBT);
    }

    public static int getCooldownEnd(ItemStack stack) {
        return stack.getOrCreateTag().getInt(COOLDOWN_NBT) + ModConfig.lapisShieldCooldown.get();
    }

    public static boolean isOnCooldown(ItemStack stack) {
        return stack.getOrCreateTag().contains(COOLDOWN_NBT);
    }

    public static void onSuccessfulUse(ItemStack stack, LivingEntity entity, float amount) {
        if (!entity.level().isClientSide()) LapisShieldItem.addCooldown(stack, entity.tickCount);
        sendCooldownUpdate(entity, entity.tickCount);

        if (entity instanceof Player player)
            player.getCooldowns().addCooldown(ModItems.LAPIS_SHIELD, ModConfig.lapisShieldCooldown.get());

        ModParticles.spawnWisps(entity.level(), entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()) / 2, entity.getZ(),
                16, new float[]{0.3f, 0.3f, 1f});

        if (amount >= 3f) {
            stack.hurtAndBreak(Math.max(1, (int) amount/2), entity, e -> e.broadcastBreakEvent(InteractionHand.OFF_HAND));
            if (stack.isEmpty())
                entity.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + entity.level().random.nextFloat() * 0.4F);
        }
    }

    public static void sendCooldownUpdate(LivingEntity entity, int cooldown) {
        if (!entity.level().isClientSide()) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            buf.writeInt(entity.getId());
            buf.writeInt(cooldown);

            for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) entity.level(), entity.blockPosition())) {
                ServerPlayNetworking.send(player, ModPackets.LAPIS_SHIELD_COOLDOWN, buf);
            }
        }
    }


    private static final ResourceLocation SHIELD_GLOW_LAYER = Sortilege.MOD.makeID("textures/models/lapis_shield_glow_layer.png");

    @Override
    public boolean shouldAddRender(ItemStack stack) {
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
