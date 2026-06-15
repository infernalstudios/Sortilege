package net.lyof.sortilege.mixin;

import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {
    /*
    // That mixin apparently makes Sinytra explode

    @Unique
    private ItemStack tryPutFilledBottle(BlockSource pointer, ItemStack empty, ItemStack filled, DispenseItemBehavior fallback) {
        empty.shrink(1);
        if (empty.isEmpty()) {
            pointer.getLevel().gameEvent(null, GameEvent.FLUID_PICKUP, pointer.getPos());
            return filled.copy();
        } else {
            if (((DispenserBlockEntity) pointer.getEntity()).addItem(filled.copy()) < 0)
                fallback.dispense(pointer, filled.copy());
            return empty;
        }
    }

    @WrapOperation(method = "dispenseFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack dispenseCauldronRefill(DispenseItemBehavior instance, BlockSource pointer, ItemStack stack,
                                            Operation<ItemStack> original) {

        ItemStack result = stack;

        if (stack.is(Items.GLASS_BOTTLE)) {
            pointer.getLevel().levelEvent(1000, pointer.getPos(), 0);
            pointer.getLevel().levelEvent(2000, pointer.getPos(), pointer.getBlockState().getValue(DispenserBlock.FACING).get3DDataValue());

            ServerLevel world = pointer.getLevel();
            BlockPos pos = pointer.getPos().relative(pointer.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(pos);

            if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
                result = this.tryPutFilledBottle(pointer, stack, PotionUtils.setPotion(new ItemStack(Items.POTION),
                        cauldron.potion), instance);
                LayeredCauldronBlock.lowerFillLevel(state, world, pos);
            }
        }
        else
            result = original.call(instance, pointer, stack);

        return result;
    }*/
}
