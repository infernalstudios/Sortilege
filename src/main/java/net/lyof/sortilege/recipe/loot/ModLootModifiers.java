package net.lyof.sortilege.recipe.loot;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public class ModLootModifiers {
    public static void register(LootTableLoadEvent event) {
        if (event.getName().getPath().startsWith("chests/") && ModConfig.limititeLootWeight.get() > 0) {
            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.LIMITITE.get()).setWeight(1))
                    .add(LootItem.lootTableItem(Items.AIR).setWeight(ModConfig.limititeLootWeight.get() - 1));

            event.getTable().addPool(poolBuilder.build());
        }

        Item fireballRod = BuiltInRegistries.ITEM.get(Sortilege.MOD.makeID("fireball_rod"));
        if (event.getName().toString().equals("minecraft:chests/nether_bridge") && fireballRod != Items.AIR) {
            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .add(LootItem.lootTableItem(fireballRod).setWeight(1))
                    .add(LootItem.lootTableItem(Items.AIR).setWeight(14));

            event.getTable().addPool(poolBuilder.build());
        }
    }
}
