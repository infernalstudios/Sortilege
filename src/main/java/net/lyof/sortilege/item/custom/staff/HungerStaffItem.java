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
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class HungerStaffItem extends AStaffItem {
    @Dependency(mod = "sortilege:hunger")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new ValueCost().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new HungerStaffItem(entry, new Properties());
        }
    }

    protected final ValueCost cost;

    public HungerStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (ValueCost) this.getEntry().getCost();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return player.getFoodData().getFoodLevel() >= this.getCost(stack, player, this.cost.getValue());
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - this.getCost(stack, player, this.cost.getValue()));
    }

    @Override
    public void appendExtraTooltip(ItemStack stack, Player player, List<Component> tooltip) {
        if (this.getCost(stack, player, cost.getValue()) > 0) {
            tooltip.add(Component.translatable("sortilege.staff.cost.hunger", this.getCost(stack, player, cost.getValue()))
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.empty());
        }
    }
}
