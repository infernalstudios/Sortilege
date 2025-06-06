package net.lyof.sortilege.mixin.accessor;

import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemDispenserBehavior.class)
public interface ItemDispenserBehaviorAccessor {
    @Invoker()
    ItemStack invokeDispenseSilently(BlockPointer pointer, ItemStack stack);
}
