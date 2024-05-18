package net.lyof.sortilege.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;

public class ModLootModifiers {
    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (id.getPath().startsWith("chests/")) {
                LootPool.Builder poolBuilder = LootPool.builder()
                        .with(ItemEntry.builder(ModItems.LIMITITE).weight(1))
                        .with(ItemEntry.builder(Items.AIR).weight(15));

                tableBuilder.pool(poolBuilder);
            }
        });
    }
}
