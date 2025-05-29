package net.lyof.sortilege.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.custom.potion.AntidotePotionItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public class ModItemGroups {
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.EXPERIENCE_BOTTLE, ModItems.LIMITITE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            if (ConfigEntries.lapisShieldEnabled) entries.addAfter(Items.SHIELD, ModItems.LAPIS_SHIELD);
            for (Item staff : ModItems.STAFFS)
                entries.addBefore(Items.TRIDENT, staff);
            if (ConfigEntries.witchHatEnabled) entries.addAfter(Items.TURTLE_HELMET, ModItems.WITCH_HAT);
            //AntidotePotionItem.fillItemGroup(entries, ModItems.ANTIDOTE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            AntidotePotionItem.fillItemGroup(entries, ModItems.ANTIDOTE);
        });
    }
}
