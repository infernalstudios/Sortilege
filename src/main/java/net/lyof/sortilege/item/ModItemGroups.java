package net.lyof.sortilege.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ModItemGroups {
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.EXPERIENCE_BOTTLE, ModItems.LIMITITE);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            KnowledgeBookItem.fillItemGroup(entries, Items.WRITABLE_BOOK);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            if (ModConfig.lapisShieldEnabled.get()) entries.addAfter(Items.SHIELD, ModItems.LAPIS_SHIELD);
            for (Item staff : ModItems.STAFFS)
                entries.addBefore(Items.TRIDENT, staff);
            if (ModConfig.witchHatEnabled.get()) entries.addAfter(Items.TURTLE_HELMET, ModItems.WITCH_HAT);
            AntidotePotionItem.fillItemGroup(entries, ModItems.ANTIDOTE);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
            AntidotePotionItem.fillItemGroup(entries, ModItems.ANTIDOTE);
        });
    }
}
