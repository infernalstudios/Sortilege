package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.block.entity.PotionCauldronBlockEntity;
import net.lyof.sortilege.mixin.accessor.ItemDispenserBehaviorAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {
    @WrapMethod(method = "registerBehavior")
    private static void registerBottleRefill(ItemConvertible provider, DispenserBehavior behavior, Operation<Void> original) {
        if (provider.asItem() != Items.GLASS_BOTTLE) {
            original.call(provider, behavior);
            return;
        }

        DispenserBehavior newBehavior = new FallibleItemDispenserBehavior() {
            private final ItemDispenserBehavior fallbackBehavior = (ItemDispenserBehavior) behavior;

            private ItemStack tryPutFilledBottle(BlockPointer pointer, ItemStack empty, ItemStack filled) {
                empty.decrement(1);
                if (empty.isEmpty()) {
                    pointer.getWorld().emitGameEvent(null, GameEvent.FLUID_PICKUP, pointer.getPos());
                    return filled.copy();
                } else {
                    if (((DispenserBlockEntity) pointer.getBlockEntity()).addToFirstFreeSlot(filled.copy()) < 0)
                        this.fallbackBehavior.dispense(pointer, filled.copy());
                    return empty;
                }
            }

            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                this.setSuccess(false);
                ServerWorld world = pointer.getWorld();
                BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                BlockState state = world.getBlockState(pos);
                
                if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
                    this.setSuccess(true);
                    ItemStack result = this.tryPutFilledBottle(pointer, stack, PotionUtil.setPotion(new ItemStack(Items.POTION), cauldron.potion));
                    LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
                    return result;
                }
                return ((ItemDispenserBehaviorAccessor) this.fallbackBehavior).invokeDispenseSilently(pointer, stack);
            }
        };
        original.call(provider, newBehavior);
    }
}
