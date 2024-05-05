package net.lyof.sortilege.item;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.item.custom.LimititeItem;
import net.lyof.sortilege.item.custom.StaffItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class ModItems {
    public static Map<String, Item> STAFFS = new HashMap<>();

    public static void register() {
        for (Pair<String, ModJsonConfigs.StaffInfo> pair : ModJsonConfigs.STAFFS) {
            String id = pair.getFirst();
            ModJsonConfigs.StaffInfo staff = pair.getSecond();
            if (FabricLoader.getInstance().isModLoaded(staff.dependency))
                STAFFS.put(id, register(id, new StaffItem(staff, new FabricItemSettings().maxCount(1))));
        }
    }

    public static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Sortilege.makeID(name), item);
    }

    public static final Item LIMITITE = register("limitite", new LimititeItem(new FabricItemSettings()));
}
