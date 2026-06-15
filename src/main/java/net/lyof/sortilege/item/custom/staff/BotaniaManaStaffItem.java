package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.botania.api.item.SortableTool;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.CustomDamageItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import java.util.List;
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
    public void appendExtraTooltip(ItemStack stack, Player player, List<Component> tooltip) {
        if (this.getCost(stack, player, cost.getValue()) > 0) {
            tooltip.add(Component.translatable("sortilege.staff.cost.mana", this.getCost(stack))
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.empty());
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
        if (!world.isClientSide() && entity instanceof Player player) {
            if (stack.getDamageValue() > 0 && ManaItemHandler.instance()
                    .requestManaExactForTool(stack, player, this.getDurabilityCost(stack) * 2, true))
                stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return ToolCommons.damageItemIfPossible(stack, amount, entity, this.getDurabilityCost(stack));
    }
}
