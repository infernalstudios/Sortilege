package net.lyof.sortilege.recipe.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;

public class ModLootModifiers {
    public static void register() {
        LootTableEvents.MODIFY.register((id, builder, source, provider) -> {
            if (id.location().getPath().startsWith("chests/") && ModConfig.limititeLootWeight.get() > 0) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .add(LootItem.lootTableItem(ModItems.LIMITITE).setWeight(1))
                        .add(LootItem.lootTableItem(Items.AIR).setWeight(ModConfig.limititeLootWeight.get() - 1));

                builder.withPool(poolBuilder);
            }

            Item fireballRod = BuiltInRegistries.ITEM.get(Sortilege.MOD.makeID("fireball_rod"));
            if (id.location().toString().equals("minecraft:chests/nether_bridge") && fireballRod != Items.AIR) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .add(LootItem.lootTableItem(fireballRod).setWeight(1))
                        .add(LootItem.lootTableItem(Items.AIR).setWeight(14));

                builder.withPool(poolBuilder);
            }
        });
    }
}
