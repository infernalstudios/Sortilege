package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.util.MathHelper;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {
    @Shadow @Nullable public abstract BlockEntity getBlockEntity(BlockPos pos);
    @Shadow public abstract boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft);
    @Shadow @Nullable private Supplier<String> currentlyGenerating;

    @WrapOperation(method = "setBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;onBlockStateChange(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    public void fillWitchCauldron(ServerLevel instance, BlockPos pos, BlockState oldBlock, BlockState newBlock, Operation<Void> original) {
        if (ModConfig.swampHutCauldrons.get() && newBlock.is(Blocks.CAULDRON) && this.currentlyGenerating != null
                && this.currentlyGenerating.get().equals("ResourceKey[minecraft:worldgen/structure / minecraft:swamp_hut]")
                && !PotionHelper.GEN_ALLOWED_POTIONS.isEmpty()) {

            this.setBlock(pos, ModBlocks.POTION_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,
                            MathHelper.randint(LayeredCauldronBlock.MIN_FILL_LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL)),
                    Block.UPDATE_IMMEDIATE, 1);
            if (this.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)
                cauldron.potion = PotionHelper.getRandomPotion();
        }
        original.call(instance, pos, oldBlock, newBlock);
    }
}
