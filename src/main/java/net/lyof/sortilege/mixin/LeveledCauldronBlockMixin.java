package net.lyof.sortilege.mixin;

import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.CauldronBrewingRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LeveledCauldronBlock.class)
public abstract class LeveledCauldronBlockMixin {
    @Shadow public abstract boolean isFull(BlockState state);

    @Shadow @Final public static IntProperty LEVEL;

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void brewItemEntity(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity instanceof ItemEntity item && this.isFull(state) && world.getBlockState(pos.down()).isIn(BlockTags.CAMPFIRES)
                && state.isOf(Blocks.WATER_CAULDRON)) {

            Optional<CauldronBrewingRecipe> optional = world.getRecipeManager().getFirstMatch(ModRecipeTypes.CAULDRON_BREWING,
                    new SimpleInventory(item.getStack()), world);

            if (optional.isPresent()) {
                world.setBlockState(pos, ModBlocks.POTION_CAULDRON.getDefaultState().with(LEVEL, 3));
                if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)
                    cauldron.potion = optional.get().output;

                world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);

                world.markDirty(pos);
                world.updateListeners(pos, state, state, 0);

                item.setStack(ItemStack.EMPTY);
                ci.cancel();
            }
        }
    }
}
