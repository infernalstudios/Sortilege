package net.lyof.sortilege.block.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
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
            INSTANCE.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
                if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
                    stack.decrement(1);
                    player.giveItemStack(PotionUtil.setPotion(Items.POTION.getDefaultStack(), cauldron.potion));

                    player.incrementStat(Stats.USE_CAULDRON);
                    player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }

                LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
                return ActionResult.success(world.isClient());
            });
        }
    }



    public PotionCauldronBlock(Settings settings) {
        super(settings.ticksRandomly(), precipitation -> true, Behavior.INSTANCE);
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

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);

        if (random.nextFloat() < 0.5 && state.get(LEVEL) != 3 && world.getBlockState(pos.down()).isIn(BlockTags.CAMPFIRES)) {
            BlockState blockState = state.cycle(LEVEL);
            world.setBlockState(pos, blockState);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (world.getBlockState(pos.down()).isIn(BlockTags.CAMPFIRES))
            world.addParticle(ParticleTypes.BUBBLE, pos.getX() + random.nextFloat(), pos.getY() + this.getFluidHeight(state) + 0.1,
                    pos.getZ() + random.nextFloat(), 0, 0, 0);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron && entity instanceof LivingEntity living) {
            for (StatusEffectInstance effect : cauldron.potion.getEffects()) {
                living.addStatusEffect(new StatusEffectInstance(effect.getEffectType(), 60));
            }
        }
    }
}
