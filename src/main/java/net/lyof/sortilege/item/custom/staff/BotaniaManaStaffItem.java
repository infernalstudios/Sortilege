package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.util.GsonHelper;
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
import vazkii.botania.common.advancements.ManaBlasterTrigger;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.item.ManaBlasterItem;
import vazkii.botania.common.item.equipment.CustomDamageItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class BotaniaManaStaffItem extends AStaffItem implements CustomDamageItem, SortableTool {
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

    protected static class Cost extends ValueCost {
        protected int valuePerDurability;

        @Override
        public ValueCost read(JsonObject json) {
            super.read(json);
            this.valuePerDurability = GsonHelper.getAsInt(json, "value_per_durability", this.getValue());
            return this;
        }

        public int getValuePerDurability() {
            return this.valuePerDurability;
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
                    .requestManaExactForTool(stack, player, this.getDurabilityCost(stack) * 2, true))
                stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return ToolCommons.damageItemIfPossible(stack, amount, entity, this.getDurabilityCost(stack));
    }

    public int getDurabilityCost(ItemStack stack) {
        return Math.max(0, cost.getValuePerDurability() - 10 * EnchantHelper.getEnchantLevel(ModEnchants.WISDOM, stack)
                + 10 * EnchantHelper.getEnchantLevel(ModEnchants.IGNORANCE_CURSE, stack));
    }

    public int getCost(ItemStack stack) {
        return Math.max(0, cost.getValue() - 100 * EnchantHelper.getEnchantLevel(ModEnchants.WISDOM, stack)
                + 100 * EnchantHelper.getEnchantLevel(ModEnchants.IGNORANCE_CURSE, stack));
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return ManaItemHandler.instance().requestManaExactForTool(stack, player, this.getCost(stack), false);
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        ManaItemHandler.instance().requestManaExactForTool(stack, player, this.getCost(stack), true);
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

        if (this.getCost(stack, player, cost.getValue()) > 0)
            tooltip.add(Component.translatable("sortilege.staff.cost.mana", this.getCost(stack)).withStyle(ChatFormatting.AQUA));
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

        BurstProperties props = new BurstProperties(this.getCost(stack), 60, 4, 0, 5, color);
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
        if (!player.isShiftKeyDown()) super.shoot(stack, player, elements, colors, direction, targetsHit);
        else {
            Level world = player.level();
            ManaBurstEntity burst = this.getBurst(player, stack, colors);

            if (!world.isClientSide()) {
                world.addFreshEntity(burst);
                ManaBlasterTrigger.INSTANCE.trigger((ServerPlayer) player, stack);
            }
        }
    }
}
