package net.lyof.sortilege.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.block.custom.PotionCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlocks {
    public static void register() {
        PotionCauldronBlock.Behavior.register();
    }

    private static Block register(String name, Block block) {
        return register(name, block, true);
    }

    private static Block register(String name, Block block, boolean withItem) {
        if (withItem)
            Registry.register(Registries.ITEM, Sortilege.makeID(name), new BlockItem(block, new Item.Settings()));
        return Registry.register(Registries.BLOCK, Sortilege.makeID(name), block);
    }


    public static final Block POTION_CAULDRON = register("potion_cauldron",
            new PotionCauldronBlock(FabricBlockSettings.copyOf(Blocks.WATER_CAULDRON)), false);
}
