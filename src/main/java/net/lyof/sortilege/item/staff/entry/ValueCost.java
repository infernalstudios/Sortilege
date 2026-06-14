package net.lyof.sortilege.item.staff.entry;

import com.google.gson.JsonObject;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.minecraft.util.GsonHelper;

public class ValueCost extends StaffEntry.Cost {
    protected int cost;

    @Override
    public ValueCost read(JsonObject json) {
        super.read(json);
        this.cost = GsonHelper.getAsInt(json, "value", 0);
        return this;
    }
}
