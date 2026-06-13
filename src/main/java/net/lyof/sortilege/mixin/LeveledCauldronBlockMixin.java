package net.lyof.sortilege.mixin;

import net.lyof.sortilege.block.ModBlocks;
import net.lyof.sortilege.block.custom.PotionCauldronBlock;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.CauldronBrewingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LayeredCauldronBlock.class)
public abstract class LeveledCauldronBlockMixin {
    @Shadow @Final public static IntegerProperty LEVEL;

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    public void brewItemEntity(BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (ModConfig.cauldronBrewingEnabled.get() &&  entity instanceof ItemEntity item && world.getBlockState(pos.below()).is(BlockTags.CAMPFIRES)
                && PotionCauldronBlock.isLit(world.getBlockState(pos.below())) && state.is(Blocks.WATER_CAULDRON)) {

            Optional<CauldronBrewingRecipe> optional = world.getRecipeManager().getRecipeFor(ModRecipeTypes.CAULDRON_BREWING,
                    new SimpleContainer(item.getItem()), world);

            if (optional.isPresent() && item.getItem().getCount() >= state.getValue(LEVEL)) {
                world.setBlockAndUpdate(pos, ModBlocks.POTION_CAULDRON.defaultBlockState().setValue(LEVEL, state.getValue(LEVEL)));
                if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)
                    cauldron.potion = optional.get().output;

                world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
                world.gameEvent(null, GameEvent.FLUID_PLACE, pos);

                world.blockEntityChanged(pos);
                world.sendBlockUpdated(pos, state, state, 0);

                item.getItem().shrink(state.getValue(LEVEL));
                ci.cancel();
            }
        }
    }
}
