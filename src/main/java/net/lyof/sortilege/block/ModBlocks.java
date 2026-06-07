package net.lyof.sortilege.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.block.custom.PotionCauldronBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ModBlocks {
    public static void register() {
        PotionCauldronBlock.Behavior.register();
    }

    private static Block register(String name, Block block) {
        return register(name, block, true);
    }

    private static Block register(String name, Block block, boolean withItem) {
        if (withItem)
            Registry.register(BuiltInRegistries.ITEM, Sortilege.makeID(name), new BlockItem(block, new Item.Properties()));
        return Registry.register(BuiltInRegistries.BLOCK, Sortilege.makeID(name), block);
    }


    public static final Block POTION_CAULDRON = register("potion_cauldron",
            new PotionCauldronBlock(FabricBlockSettings.copyOf(Blocks.WATER_CAULDRON)), false);
}
