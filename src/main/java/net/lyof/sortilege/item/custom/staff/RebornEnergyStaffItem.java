package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import reborncore.common.powerSystem.PowerSystem;
import reborncore.common.util.ItemUtils;
import reborncore.common.util.StringUtils;
import team.reborn.energy.api.base.SimpleEnergyItem;

import java.util.List;
import java.util.function.BiConsumer;

public class RebornEnergyStaffItem extends AStaffItem implements SimpleEnergyItem {
    @Dependency(mod = "reborncore:energy")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new Cost().read(json);
        }

        @Override
        public void register(StaffEntry entry, BiConsumer<String, AStaffItem> registrar) {
            registrar.accept(entry.getID(), new RebornEnergyStaffItem(entry, new Properties()));
        }
    }

    public static class Cost extends StaffEntry.Cost {
        protected int energy;
        protected int transferRate;

        @Override
        public StaffEntry.Cost read(JsonObject json) {
            super.read(json);
            this.energy = GsonHelper.getAsInt(json, "energy");
            this.transferRate = GsonHelper.getAsInt(json, "energy_transfer_rate", 64);
            return this;
        }

        public int getEnergy() {
            return this.energy;
        }

        public int getEnergyTransferRate() {
            return this.transferRate;
        }
    }

    protected final Cost cost;

    public RebornEnergyStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties.durability(-1 ));
        this.cost = (Cost) this.getEntry().getCost();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return this.getStoredEnergy(stack) >= this.getCost(stack, player, cost.getEnergy());
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        this.tryUseEnergy(stack, this.getCost(stack, player, cost.getEnergy()));
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        // Taken from RebornCore
        tooltip.add(Component.literal(PowerSystem.getLocalizedPowerNoSuffix(this.getStoredEnergy(stack)))
                .append("/")
                .append(PowerSystem.getLocalizedPower(this.getEnergyCapacity(stack)))
                .withStyle(ChatFormatting.GOLD));

        if (Screen.hasControlDown()) {
            int percentage = (int) (100 * this.getStoredEnergy(stack) / this.getEnergyCapacity(stack));
            tooltip.add(StringUtils.getPercentageText(percentage)
                    .append(" ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(I18n.get("reborncore.gui.tooltip.power_charged")));

            double inputRate = this.getEnergyMaxInput(stack);
            if (inputRate != 0)
                tooltip.add(Component.empty()
                        .append(I18n.get("reborncore.tooltip.energy.inputRate"))
                        .append(" : ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(PowerSystem.getLocalizedPower(inputRate))
                        .withStyle(ChatFormatting.GOLD));
        }
        if (this.getCost(stack, player, cost.getEnergy()) > 0) {
            tooltip.add(Component.translatable("tooltip.sortilege.staff.cost.energy",
                    PowerSystem.getLocalizedPower(this.getCost(stack, player, cost.getEnergy()))).withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return entry.getTier().getUses();
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return cost.getEnergyTransferRate();
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return !ItemUtils.isEqualIgnoreEnergy(oldStack, newStack);
    }

    @Override
    public boolean allowContinuingBlockBreaking(Player player, ItemStack oldStack, ItemStack newStack) {
        return ItemUtils.isEqualIgnoreEnergy(oldStack, newStack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round((float) this.getStoredEnergy(stack) / this.getEnergyCapacity(stack) * 13.0f);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ItemUtils.getColorForDurabilityBar(stack);
    }
}
