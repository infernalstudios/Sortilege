package net.lyof.sortilege.block.custom;

import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PotionCauldronBlock extends LeveledCauldronBlock implements BlockEntityProvider {
    public static class Behavior {
        public static final Map<Item, CauldronBehavior> INSTANCE = new HashMap<>() {
            @Override
            public CauldronBehavior get(Object key) {
                if (!this.containsKey(key)) return (state, world, pos, player, hand, stack) -> ActionResult.PASS;
                return super.get(key);
            }
        };

        public static void register() {

        }
    }



    public PotionCauldronBlock(Settings settings) {
        super(settings, precipitation -> true, Behavior.INSTANCE);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PotionCauldronBlockEntity(pos, state);
    }

    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity entity = world.getBlockEntity(pos);
        return entity != null && entity.onSyncedBlockEvent(type, data);
    }
}
