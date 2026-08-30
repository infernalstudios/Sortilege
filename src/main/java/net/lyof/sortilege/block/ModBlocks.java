package net.lyof.sortilege.block;

import net.lcc.sollib.api.common.registry.holder.BlockHolder;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.block.custom.PotionCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static void register() {}

    public static final Block POTION_CAULDRON = Sortilege.MOD.register(BlockHolder.class, "potion_cauldron",
            () -> new PotionCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WATER_CAULDRON))).get();
}
