package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
    public boolean canShoot(ItemStack stack, Player player) {
        return super.canShoot(stack, player) || player.getFoodData().getFoodLevel() >= this.cost.getValue();
    }

    @Override
    public void applyCost(ItemStack stack, Player player) {
        if (this.getOvercharge(stack) <= 0 || !this.cost.getOvercharge().ignoreCost())
            player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - this.cost.getValue());
        super.applyCost(stack, player);
    }
}
