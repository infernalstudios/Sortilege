package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(ChunkRegion.class)
public abstract class ChunkRegionMixin {
    @Shadow @Nullable private Supplier<String> currentlyGeneratingStructureName;

    @Shadow public abstract boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth);

    @Shadow @Nullable public abstract BlockEntity getBlockEntity(BlockPos pos);

    @WrapOperation(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onBlockChanged(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V"))
    public void fillWitchCauldron(ServerWorld instance, BlockPos pos, BlockState oldBlock, BlockState newBlock, Operation<Void> original) {
        if (ConfigEntries.fillSwampHutCauldrons && newBlock.isOf(Blocks.CAULDRON) && this.currentlyGeneratingStructureName != null
                && this.currentlyGeneratingStructureName.get().equals("ResourceKey[minecraft:worldgen/structure / minecraft:swamp_hut]")
                && !PotionHelper.GEN_ALLOWED_POTIONS.isEmpty()) {

            this.setBlockState(pos, ModBlocks.POTION_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL,
                            MathHelper.randint(LeveledCauldronBlock.MIN_LEVEL, LeveledCauldronBlock.MAX_LEVEL)),
                    Block.REDRAW_ON_MAIN_THREAD, 1);
            if (this.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)
                cauldron.potion = PotionHelper.getRandomPotion();
        }
        original.call(instance, pos, oldBlock, newBlock);
    }
}
