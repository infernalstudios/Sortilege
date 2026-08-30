package net.lyof.sortilege.block.custom;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.lyof.sortilege.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PotionCauldronBlock extends LayeredCauldronBlock implements EntityBlock {
    public static class Behavior {
        public static final CauldronInteraction.InteractionMap INSTANCE = create();

        public static CauldronInteraction.InteractionMap create() {
            Object2ObjectOpenHashMap<Item, CauldronInteraction> map = new Object2ObjectOpenHashMap<>();
            map.defaultReturnValue((state, level, pos, player, hand, stack) -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);


            map.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
                if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
                    stack.shrink(1);
                    player.addItem(PotionContents.createItemStack(Items.POTION, cauldron.potion));

                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }

                LayeredCauldronBlock.lowerFillLevel(state, world, pos);
                return ItemInteractionResult.sidedSuccess(world.isClientSide());
            });

            CauldronInteraction.InteractionMap interactionMap = new CauldronInteraction.InteractionMap("sortilege:potion", map);
            CauldronInteraction.INTERACTIONS.put("sortilege:potion", interactionMap);
            return interactionMap;
        }
    }


    public static int getBlockColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
        if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron && tintIndex == 0)
            return (int) cauldron.getRenderData();
        return 16253176;
    }

    public static boolean isLit(BlockState state) {
        return state.getProperties().contains(CampfireBlock.LIT) ? state.getValue(CampfireBlock.LIT) : true;
    }


    public PotionCauldronBlock(BlockBehaviour.Properties settings) {
        super(Biome.Precipitation.NONE, Behavior.INSTANCE, settings.randomTicks());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotionCauldronBlockEntity(pos, state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int type, int data) {
        super.triggerEvent(state, world, pos, type, data);
        BlockEntity entity = world.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(type, data);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.randomTick(state, world, pos, random);

        if (random.nextFloat() < 0.5 && state.getValue(LEVEL) != 3 && world.getBlockState(pos.below()).is(ModTags.Blocks.REFILLS_CAULDRONS)
                && isLit(world.getBlockState(pos.below()))) {
            BlockState blockState = state.cycle(LEVEL);
            world.setBlockAndUpdate(pos, blockState);
        }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);

        if (world.getBlockState(pos.below()).is(BlockTags.CAMPFIRES) && isLit(world.getBlockState(pos.below())))
            world.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + this.getRandomOffset(random), pos.getY() + this.getContentHeight(state) + 0.1,
                    pos.getZ() + this.getRandomOffset(random), 0, 0, 0);

        if (random.nextFloat() < 0.3) {
            int rgb = getBlockColor(state, world, pos, 0);
            world.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, rgb), pos.getX() + this.getRandomOffset(random), pos.getY() + this.getContentHeight(state) + 0.1,
                    pos.getZ() + this.getRandomOffset(random), 0, 0, 0);
        }
    }

    private float getRandomOffset(RandomSource random) {
        return random.nextFloat()*0.8f + 0.2f;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!(world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)) return;

        if (entity instanceof LivingEntity living) {
            for (MobEffectInstance effect : cauldron.potion.value().getEffects()) {
                living.addEffect(new MobEffectInstance(effect.getEffect(),
                        effect.getEffect().value().isInstantenous() ? 1 : 100));
            }
        }

        if (entity instanceof ItemEntity item) {
            /*
            Optional<CauldronBrewingRecipe> optional = world.getRecipeManager().getFirstMatch(ModRecipeTypes.CAULDRON_BREWING,
                    new SimpleInventory(item.getStack()), world);

            if (optional.isPresent() && item.getStack().getCount() >= state.get(LEVEL) && cauldron.potion != optional.get().output) {
                cauldron.potion = optional.get().output;

                world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);

                world.markDirty(pos);
                world.updateListeners(pos, state, state, 0);

                item.getStack().decrement(state.get(LEVEL));
            }*/
            if (item.getItem().is(ModTags.Items.REFILLS_CAULDRONS) && !this.isFull(state)) {
                world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
                world.gameEvent(null, GameEvent.FLUID_PLACE, pos);

                world.setBlockAndUpdate(pos, state.cycle(LEVEL));
                world.blockEntityChanged(pos);
                world.sendBlockUpdated(pos, state, state, 0);

                item.getItem().shrink(1);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        return Blocks.CAULDRON.getCloneItemStack(world, pos, state);
    }
}
