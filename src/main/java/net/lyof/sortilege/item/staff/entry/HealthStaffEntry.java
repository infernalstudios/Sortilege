package net.lyof.sortilege.item.staff.entry;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;

@Dependency(mod = "sortilege:health")
public class HealthStaffEntry implements IStaffEntryReader {
    @Override
    public StaffEntry.Cost readCost(JsonObject json) {
        return new ValueCost().read(json);
    }

    @Override
    public StaffEntry.Effects readEffects(JsonObject json) {
        return new StaffEntry.Effects().read(json);
    }

    @Override
    public StaffEntry.Display readDisplay(JsonObject json) {
        return new StaffEntry.Display().read(json);
    }

    @Override
    public AStaffItem make(StaffEntry entry) {
        return null;
    }
}
