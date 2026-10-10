package net.lyof.sortilege.item;

import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.custom.AntidotePotionItem;
import net.lyof.sortilege.item.custom.KnowledgeBookItem;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ModItemGroups {
    public static final Set<String> STAFF_BLACKLIST = new HashSet<>();

    public static void addAfter(BuildCreativeModeTabContentsEvent event, ItemLike target, ItemLike item) {
        event.insertAfter(target.asItem().getDefaultInstance(), item.asItem().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void addBefore(BuildCreativeModeTabContentsEvent event, ItemLike target, ItemLike item) {
        event.insertBefore(target.asItem().getDefaultInstance(), item.asItem().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void register(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) {
            addAfter(event, Items.EXPERIENCE_BOTTLE, ModItems.LIMITITE.get());
        } else if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            KnowledgeBookItem.fillItemGroup(event, Items.WRITABLE_BOOK);
        } else if (event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            if (ModConfig.lapisShieldEnabled.get()) addAfter(event, Items.SHIELD, ModItems.LAPIS_SHIELD.get());
            for (Supplier<AStaffItem> staff : ModItems.STAFFS)
                if (!STAFF_BLACKLIST.contains(staff.get().getName())) addBefore(event, Items.TRIDENT, staff.get());
            if (ModConfig.witchHatEnabled.get()) addAfter(event, Items.TURTLE_HELMET, ModItems.WITCH_HAT.get());
            AntidotePotionItem.fillItemGroup(event, ModItems.ANTIDOTE.get());
        } else if (event.getTabKey().equals(CreativeModeTabs.FOOD_AND_DRINKS)) {
            AntidotePotionItem.fillItemGroup(event, ModItems.ANTIDOTE.get());
        }
    }
}
