package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HealthStaffItem extends AStaffItem {
    @Dependency(mod = "sortilege:health")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new ValueCost().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new HealthStaffItem(entry, new Properties());
        }
    }

    protected final ValueCost cost;

    public HealthStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (ValueCost) this.getEntry().getCost();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return player.getHealth() > this.getCost(stack, player, this.cost.getValue());
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        player.hurt(player.damageSources().wither(), this.getCost(stack, player, this.cost.getValue()));
    }
}
