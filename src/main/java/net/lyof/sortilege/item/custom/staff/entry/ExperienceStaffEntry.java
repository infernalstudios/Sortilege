package net.lyof.sortilege.item.custom.staff.entry;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.staff.AStaffItem;
import net.lyof.sortilege.item.custom.staff.IStaffEntryReader;
import net.lyof.sortilege.item.custom.staff.StaffEntry;

@Dependency(mod = "sortilege:experience")
public class ExperienceStaffEntry implements IStaffEntryReader {
    @Override
    public StaffEntry.Cost readCost(JsonObject json) {
        return new Cost();
    }

    @Override
    public StaffEntry.Effects readEffects(JsonObject json) {
        return new StaffEntry.Effects();
    }

    @Override
    public StaffEntry.Display readDisplay(JsonObject json) {
        return new StaffEntry.Display();
    }

    @Override
    public AStaffItem make(StaffEntry entry) {
        return null;
    }

    public static class Cost extends StaffEntry.Cost {

    }
}
