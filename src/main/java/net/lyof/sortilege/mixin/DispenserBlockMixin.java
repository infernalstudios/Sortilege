package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {
    /*
    @WrapMethod(method = "registerBehavior")
    private static void registerBottleRefill(ItemConvertible provider, DispenserBehavior behavior, Operation<Void> original) {
        if (provider.asItem() != Items.GLASS_BOTTLE) {
            original.call(provider, behavior);
            return;
        }

        DispenserBehavior newBehavior = new FallibleItemDispenserBehavior() {
            private final ItemDispenserBehavior fallbackBehavior = (ItemDispenserBehavior) behavior;



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
    }*/

    @Unique
    private ItemStack tryPutFilledBottle(BlockPointer pointer, ItemStack empty, ItemStack filled, DispenserBehavior fallback) {
        empty.decrement(1);
        if (empty.isEmpty()) {
            pointer.getWorld().emitGameEvent(null, GameEvent.FLUID_PICKUP, pointer.getPos());
            return filled.copy();
        } else {
            if (((DispenserBlockEntity) pointer.getBlockEntity()).addToFirstFreeSlot(filled.copy()) < 0)
                fallback.dispense(pointer, filled.copy());
            return empty;
        }
    }

    @WrapOperation(method = "dispense", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/dispenser/DispenserBehavior;dispense(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack dispenseCauldronRefill(DispenserBehavior instance, BlockPointer pointer, ItemStack stack,
                                            Operation<ItemStack> original) {

        ItemStack result = stack;

        if (stack.isOf(Items.GLASS_BOTTLE)) {
            pointer.getWorld().syncWorldEvent(1000, pointer.getPos(), 0);
            pointer.getWorld().syncWorldEvent(2000, pointer.getPos(), pointer.getBlockState().get(DispenserBlock.FACING).getId());

            ServerWorld world = pointer.getWorld();
            BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
            BlockState state = world.getBlockState(pos);

            if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
                result = this.tryPutFilledBottle(pointer, stack, PotionUtil.setPotion(new ItemStack(Items.POTION),
                        cauldron.potion), instance);
                LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
            }
        }
        else
            result = original.call(instance, pointer, stack);

        return result;
    }
}
