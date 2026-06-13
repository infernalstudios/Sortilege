package net.lyof.sortilege.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.lcc.sollib.api.common.registry.holder.BlockHolder;
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


    public static final Block POTION_CAULDRON = Sortilege.MOD.register(BlockHolder.class, "potion_cauldron",
            () -> new PotionCauldronBlock(FabricBlockSettings.copyOf(Blocks.WATER_CAULDRON))).get();
}
