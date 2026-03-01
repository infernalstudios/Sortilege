package net.lyof.sortilege.item.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.rendering.AddedRenderItem;
import net.lyof.sortilege.item.custom.rendering.MockItemRenderer;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModPackets;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LapisShieldItem extends Item implements Equipment, AddedRenderItem {
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

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.modifiers.offhand").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.sortilege.lapis_shield.desc", ConfigEntries.lapisShieldCooldown / 20f)
                .formatted(Formatting.GRAY));
    }

    private static final String COOLDOWN_NBT = Sortilege.MOD_ID + "_LastUse";

    public static void addCooldown(ItemStack stack, int time) {
        stack.getOrCreateNbt().putInt(COOLDOWN_NBT, time);
    }

    public static void removeCooldown(ItemStack stack) {
        stack.getOrCreateNbt().remove(COOLDOWN_NBT);
    }

    public static int getCooldownEnd(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(COOLDOWN_NBT) + ConfigEntries.lapisShieldCooldown;
    }

    public static boolean isOnCooldown(ItemStack stack) {
        return stack.getOrCreateNbt().contains(COOLDOWN_NBT);
    }

    public static void onSuccessfulUse(ItemStack stack, LivingEntity entity, float amount) {
        if (!entity.getWorld().isClient()) LapisShieldItem.addCooldown(stack, entity.age);
        sendCooldownUpdate(entity, entity.age);

        if (entity instanceof PlayerEntity player)
            player.getItemCooldownManager().set(ModItems.LAPIS_SHIELD, ConfigEntries.lapisShieldCooldown);

        ModParticles.spawnWisps(entity.getWorld(), entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()) / 2, entity.getZ(),
                16, new float[]{0.3f, 0.3f, 1f});

        if (amount >= 3f) {
            stack.damage(Math.max(1, (int) amount/2), entity, e -> e.sendToolBreakStatus(Hand.OFF_HAND));
            if (stack.isEmpty())
                entity.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + entity.getWorld().random.nextFloat() * 0.4F);
        }
    }

    public static void sendCooldownUpdate(LivingEntity entity, int cooldown) {
        if (!entity.getWorld().isClient()) {
            PacketByteBuf buf = PacketByteBufs.create();

            buf.writeInt(entity.getId());
            buf.writeInt(cooldown);

            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) entity.getWorld(), entity.getBlockPos())) {
                ServerPlayNetworking.send(player, ModPackets.LAPIS_SHIELD_COOLDOWN, buf);
            }
        }
    }


    private static final Identifier SHIELD_GLOW_LAYER = Sortilege.makeID("textures/models/lapis_shield_glow_layer.png");

    @Override
    public boolean shouldRender(ItemStack stack) {
        return !LapisShieldItem.isOnCooldown(stack);
    }

    @Override
    public void render(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.scale(1.005f, 1.005f, 1.005f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.translate(-6 * PX_UNIT, -4 * PX_UNIT, -2 * PX_UNIT);

        MockItemRenderer.renderItem(matrices, vertexConsumers, -1, SHIELD_GLOW_LAYER);
    }
}
