package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
    public boolean canShoot(ItemStack stack, Player player) {
        return super.canShoot(stack, player) || XPHelper.hasXP(player, this.cost.getValue());
    }

    @Override
    public void applyCost(ItemStack stack, Player player) {
        if (this.getOvercharge(stack) <= 0 || !this.cost.getOvercharge().ignoreCost())
            player.giveExperiencePoints(-this.cost.getValue());
        super.applyCost(stack, player);
    }
}
