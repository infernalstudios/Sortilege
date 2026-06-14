package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;

import java.util.Iterator;
import java.util.ServiceLoader;

public interface IStaffEntryReader {
    StaffEntry.Cost readCost(JsonObject json);
    StaffEntry.Effects readEffects(JsonObject json);
    StaffEntry.Display readDisplay(JsonObject json);
    AStaffItem make(StaffEntry entry);

    static IStaffEntryReader getFor(String type) {
        Iterator<IStaffEntryReader> iterator = ServiceLoader.load(IStaffEntryReader.class).iterator();
        while (iterator.hasNext()) {
            try {
                IStaffEntryReader loadedService = iterator.next();

                Dependency dependency = loadedService.getClass().getAnnotation(Dependency.class);
                if (dependency != null && !dependency.mod().equals(type))
                    continue;

                return loadedService;
            } catch (Exception ignored) {}
        }

        Sortilege.log().error(type + " is not a valid Staff type");
        return null;
    }
}
