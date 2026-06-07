package net.lyof.sortilege.recipe.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;

public class ModLootModifiers {
    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (id.getPath().startsWith("chests/") && ConfigEntries.limititeLootWeight > 0) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .add(LootItem.lootTableItem(ModItems.LIMITITE).setWeight(1))
                        .add(LootItem.lootTableItem(Items.AIR).setWeight(ConfigEntries.limititeLootWeight - 1));

                tableBuilder.withPool(poolBuilder);
            }
        });
    }
}
