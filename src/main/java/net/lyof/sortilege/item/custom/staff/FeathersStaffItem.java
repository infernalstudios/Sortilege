package net.lyof.sortilege.item.custom.staff;

import com.elenai.feathers.api.FeathersHelper;
import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FeathersStaffItem extends AStaffItem {
    @Dependency(mod = "feathers:feathers")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new ValueCost().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new FeathersStaffItem(entry, new Item.Properties());
        }
    }


    protected final ValueCost cost;

    public FeathersStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (ValueCost) this.getEntry().getCost();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return player instanceof ServerPlayer serverPlayer
                ? FeathersHelper.getFeathers(serverPlayer) >= this.getCost(stack, player, cost.getValue())
                : FeathersHelper.getFeathers() >= this.getCost(stack, player, cost.getValue());
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        if (player instanceof ServerPlayer serverPlayer)
            FeathersHelper.spendFeathers(serverPlayer, this.getCost(stack, player, cost.getValue()));
        else
            FeathersHelper.spendFeathers(this.getCost(stack, player, cost.getValue()));
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getValue()) > 0)
            tooltip.add(Component.translatable("tooltip.sortilege.staff.cost.feathers", this.getCost(stack, player, cost.getValue())).withStyle(ChatFormatting.AQUA));
    }
}
