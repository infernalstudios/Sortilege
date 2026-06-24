package net.lyof.sortilege.item.staff;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.minecraft.resources.ResourceLocation;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;

public interface IStaffEntryReader {
    default StaffEntry.Cost readCost(JsonObject json) {
        return new StaffEntry.Cost().read(json);
    }
    default StaffEntry.Effects readEffects(JsonObject json) {
        return new StaffEntry.Effects().read(json);
    }
    default StaffEntry.Display readDisplay(JsonObject json) {
        return new StaffEntry.Display().read(json);
    }
    void register(StaffEntry entry, BiConsumer<String, AStaffItem> registrar);
    default void finalize(AStaffItem staff) {}


    default ResourceLocation getType() {
        Dependency dependency = this.getClass().getAnnotation(Dependency.class);
        return dependency == null ? null : Identifier.of(dependency.mod());
    }

    static IStaffEntryReader getFor(String type) {
        Iterator<IStaffEntryReader> iterator = ServiceLoader.load(IStaffEntryReader.class).iterator();
        while (iterator.hasNext()) {
            try {
                IStaffEntryReader loadedService = iterator.next();

                ResourceLocation id = loadedService.getType();
                if (id == null) continue;
                if (FabricLoader.getInstance().isModLoaded(id.getNamespace()) && type.equals(id.toString()))
                    return loadedService;

            } catch (Throwable ignored) {}
        }

        Sortilege.log().error(type + " is not a valid Staff type");
        return null;
    }
}
