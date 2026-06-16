package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ExperienceStaffItem extends AStaffItem {
    @Dependency(mod = "sortilege:experience")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new ValueCost().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new ExperienceStaffItem(entry, new Item.Properties());
        }
    }

    protected final ValueCost cost;

    public ExperienceStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (ValueCost) this.getEntry().getCost();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return XPHelper.hasXP(player, this.getCost(stack, player, cost.getValue()));
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        player.giveExperiencePoints(-this.getCost(stack, player, cost.getValue()));
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getValue()) > 0)
            tooltip.add(Component.translatable("sortilege.staff.cost.experience", this.getCost(stack, player, cost.getValue())).withStyle(ChatFormatting.GREEN));
    }
}
