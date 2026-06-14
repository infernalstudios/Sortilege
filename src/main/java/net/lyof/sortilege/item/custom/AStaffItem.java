package net.lyof.sortilege.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.lcc.sollib.api.client.render.MockItemRenderer;
import net.lcc.sollib.api.client.render.item.IAddedBarItem;
import net.lcc.sollib.api.client.render.item.IAddedRenderItem;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AStaffItem extends TieredItem implements DyeableLeatherItem, IAddedRenderItem, IAddedBarItem {
    private static final float[] COLOR_NONE = new float[]{1f, 1f, 1f};
    private static final float[] COLOR_ENCHANTED = new float[]{0.7f, 0f, 1f};

    protected final StaffEntry entry;

    public AStaffItem(StaffEntry entry, Properties properties) {
        super(entry.getTier(), entry.getTier().isFireproof() ? properties.fireResistant() : properties);
        this.entry = entry;
    }

    private static final ResourceLocation COLOR_OVERLAY = Sortilege.MOD.makeID("textures/models/staff/glint.png");

    @Override
    public boolean shouldAddRender(ItemStack stack) {
        return this.hasCustomColor(stack) && !stack.is(ModTags.Items.NO_DYE_OVERLAY_STAFFS);
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext context, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        matrices.scale(1.005f, 1.005f, 1.005f);
        matrices.translate(0, 0.995, 0.4975);
        matrices.mulPose(Axis.XP.rotationDegrees(180));

        MockItemRenderer.renderTintedItem(matrices, vertexConsumers, light, COLOR_OVERLAY, this.getColor(stack));
    }

    @Override
    public boolean shouldAddBarRender(ItemStack itemStack) {
        return true;
    }

    @Override
    public float getAddedBarFullness(ItemStack itemStack) {
        return 1;
    }

    @Override
    public int getAddedBarColor(ItemStack itemStack) {
        return this.entry.getCost().getOvercharge().getColor();
    }

/*
    public abstract boolean canShoot(ItemStack stack, Player player);

    public abstract void onShoot(ItemStack stack, Player player);

    public abstract void shoot(ItemStack stack, Player player);*/
}