package net.lyof.sortilege.block.custom;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.lyof.sortilege.recipe.ModRecipeTypes;
import net.lyof.sortilege.recipe.brewing.CauldronBrewingRecipe;
import net.lyof.sortilege.util.PotionHelper;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public static int getBlockColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)
            return (int) cauldron.getRenderData();
        return 16253176;
    }


    public static final BooleanProperty REFRESH = BooleanProperty.of("refresh_render");

    public PotionCauldronBlock(Settings settings) {
        super(settings.ticksRandomly(), precipitation -> true, Behavior.INSTANCE);
        this.setDefaultState(this.getDefaultState().with(REFRESH, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(REFRESH);
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
            world.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + this.getRandomOffset(random), pos.getY() + this.getFluidHeight(state) + 0.1,
                    pos.getZ() + this.getRandomOffset(random), 0, 0, 0);
    }

    private float getRandomOffset(Random random) {
        return random.nextFloat()*0.8f + 0.2f;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)) return;

        if (entity instanceof LivingEntity living) {
            for (StatusEffectInstance effect : cauldron.potion.getEffects()) {
                living.addStatusEffect(new StatusEffectInstance(effect.getEffectType(), 100));
            }
        }

        if (entity instanceof ItemEntity item) {
            Optional<CauldronBrewingRecipe> optional = world.getRecipeManager().getFirstMatch(ModRecipeTypes.CAULDRON_BREWING,
                    new SimpleInventory(item.getStack()), world);

            if (optional.isPresent() && item.getStack().getCount() >= state.get(LEVEL) && cauldron.potion != optional.get().output) {
                cauldron.potion = optional.get().output;

                world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);

                world.markDirty(pos);
                world.updateListeners(pos, state, state, 0);

                item.getStack().decrement(state.get(LEVEL));
            }
        }
    }
}
