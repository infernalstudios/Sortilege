package net.lyof.sortilege.item.staff.entry;

import com.google.gson.JsonObject;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.minecraft.util.GsonHelper;

public class ValueCost extends StaffEntry.Cost {
    protected int value;

    @Override
    public ValueCost read(JsonObject json) {
        super.read(json);
        if (json.has("value"))
            this.value = GsonHelper.getAsInt(json, "value");
        return this;
    }

    public int getValue() {
        return this.value;
    }
}
