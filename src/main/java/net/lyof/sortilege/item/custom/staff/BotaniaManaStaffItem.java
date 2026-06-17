package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.particle.ModParticles;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.item.SortableTool;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.LensEffectItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.BotaniaParticles;
import vazkii.botania.common.advancements.ManaBlasterTrigger;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.item.equipment.CustomDamageItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class BotaniaManaStaffItem extends AStaffItem implements CustomDamageItem {
    @Dependency(mod = "botania:mana")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new Cost().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new BotaniaManaStaffItem(entry, new Properties());
        }
    }

    protected static class Cost extends StaffEntry.Cost {
        protected int mana;
        protected int manaPerDurability;

        @Override
        public Cost read(JsonObject json) {
            super.read(json);
            this.mana = GsonHelper.getAsInt(json, "mana", 100);
            this.manaPerDurability = GsonHelper.getAsInt(json, "mana_per_durability", this.getMana());
            return this;
        }

        public int getMana() {
            return this.mana;
        }

        public int getManaPerDurability() {
            return this.manaPerDurability;
        }
    }


    protected final Cost cost;

    public BotaniaManaStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (Cost) this.getEntry().getCost();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
        if (!world.isClientSide() && entity instanceof Player player) {
            if (stack.getDamageValue() > 0 && ManaItemHandler.instance()
                    .requestManaExactForTool(stack, player, this.getDurabilityMana(stack) * 2, true))
                stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return ToolCommons.damageItemIfPossible(stack, amount, entity, this.getDurabilityMana(stack));
    }

    public int getDurabilityMana(ItemStack stack) {
        return this.getCost(stack, null, cost.getManaPerDurability());
    }

    public int getMana(ItemStack stack) {
        return this.getCost(stack, null, cost.getMana());
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return ManaItemHandler.instance().requestManaExactForTool(stack, player, this.getMana(stack), false);
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        ManaItemHandler.instance().requestManaExactForTool(stack, player, this.getMana(stack), true);
    }

    @Override
    public void appendTooltipAbilities(ItemStack stack, Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable("item.sortilege.staff.botania.desc").withStyle(ChatFormatting.GRAY));
        ItemStack lens = this.getLens(stack);
        if (!lens.isEmpty())
            tooltip.add(Component.empty().withStyle(ChatFormatting.GRAY)
                    .append(" (").append(lens.getHoverName().copy().withStyle(ChatFormatting.GREEN)).append(")"));

        super.appendTooltipAbilities(stack, player, tooltip);
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getMana(stack) > 0)
            tooltip.add(Component.translatable("sortilege.staff.cost.mana", this.getMana(stack)).withStyle(ChatFormatting.AQUA));
    }

    @Override
    public ParticleType<?> getParticle() {
        if (this.getEntry().getDisplay().getParticle() == null) return BotaniaParticles.WISP;
        return super.getParticle();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean shouldAddRender(ItemStack stack) {
        return super.shouldAddRender(stack) || !this.getLens(stack).isEmpty();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(ItemStack stack, ItemDisplayContext context, PoseStack matrices, MultiBufferSource vertexConsumers,
                       int light, int overlay) {
        if (super.shouldAddRender(stack))
            super.render(stack, context, matrices, vertexConsumers, light, overlay);

        if (context == ItemDisplayContext.GUI) {
            matrices.pushPose();
            matrices.translate(0.19, 0.19, 0);
            matrices.scale(0.5f, 0.5f, 0.5f);

            Minecraft.getInstance().getItemRenderer().renderStatic(this.getLens(stack),
                    context, light, overlay, matrices, vertexConsumers, Minecraft.getInstance().level, 0);

            matrices.popPose();
        }
    }

    protected static final String LENS_NBT = "sorti_Lens";

    public ItemStack getLens(ItemStack stack) {
        return ItemStack.of(stack.getOrCreateTag().getCompound(LENS_NBT));
    }

    public void setLens(ItemStack stack, ItemStack lens) {
        if (lens.isEmpty())
            stack.removeTagKey(LENS_NBT);
        else
            stack.getOrCreateTag().put(LENS_NBT, lens.save(new CompoundTag()));
    }

    protected BurstProperties getBurstProps(Player player, ItemStack stack, List<float[]> colors) {
        float[] c = MathHelper.randi(colors);
        int color = FastColor.ARGB32.color(255, (int) (c[0] * 255), (int) (c[1] * 255), (int) (c[2] * 255));

        float speed = stack.is(ModTags.Items.TERRA_ITEMS) ? 7 : 5;
        BurstProperties props = new BurstProperties(this.getMana(stack), 60, 4, 0, speed, color);
        ItemStack lens = this.getLens(stack);
        if (!lens.isEmpty())
            ((LensEffectItem) lens.getItem()).apply(lens, props, player.level());

        return props;
    }

    protected ManaBurstEntity getBurst(Player player, ItemStack stack, List<float[]> colors) {
        ManaBurstEntity burst = new ManaBurstEntity(player);
        BurstProperties props = this.getBurstProps(player, stack, colors);
        burst.setSourceLens(this.getLens(stack));

        burst.setColor(props.color);
        burst.setMana(props.maxMana);
        burst.setStartingMana(props.maxMana);
        burst.setMinManaLoss(props.ticksBeforeManaLoss);
        burst.setManaLossPerTick(props.manaLossPerTick);
        burst.setGravity(props.gravity);
        burst.setDeltaMovement(burst.getDeltaMovement().scale(props.motionModifier));
        return burst;
    }

    @Override
    public void shoot(ItemStack stack, Player player, Set<ElementalStaffEnchantment> elements, List<float[]> colors, Vec3 direction, List<LivingEntity> targetsHit) {
        if (!player.isShiftKeyDown()) {
            super.shoot(stack, player, elements, colors, direction, targetsHit);

            if (stack.is(ModTags.Items.TERRA_ITEMS)) {
                int t = (int) player.level().getDayTime();
                if (player.level().isClientSide()) t += 1;

                double dx = 0.2*Mth.sin(t);
                double dy = 0.2*Mth.sin(t * 2);
                double dz = 0.2*Mth.sin(t * 3);

                super.shoot(stack, player, elements, colors, direction.subtract(dx, dy, dz), targetsHit);
                super.shoot(stack, player, elements, colors, direction.add(dx, dy, dz), targetsHit);
            }
        } else {
            Level world = player.level();
            ManaBurstEntity burst = this.getBurst(player, stack, colors);

            if (!world.isClientSide()) {
                world.addFreshEntity(burst);
                ManaBlasterTrigger.INSTANCE.trigger((ServerPlayer) player, stack);
            }
        }
    }
}
