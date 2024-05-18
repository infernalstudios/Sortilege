package net.lyof.sortilege.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lyof.sortilege.item.custom.potion.AntidotePotionItem;
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
            entries.add(ModItems.WITCH_HAT);
            AntidotePotionItem.fillItemGroup(entries, ModItems.ANTIDOTE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            AntidotePotionItem.fillItemGroup(entries, ModItems.ANTIDOTE);
        });
    }
}
