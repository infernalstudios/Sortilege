package net.lyof.sortilege.item;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ModJsonConfigs;
import net.lyof.sortilege.item.custom.StaffItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;

public class ModItemGroups {
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(ModItems.LIMITITE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            for (Item staff : ModItems.STAFFS)
                entries.add(staff);
        });
    }
}
