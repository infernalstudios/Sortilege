package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.item.custom.StaffItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
public class ShieldItemMixin {
    @WrapMethod(method = "use")
    public TypedActionResult<ItemStack> handleStaffUse(World world, PlayerEntity user, Hand hand,
                                                       Operation<TypedActionResult<ItemStack>> original) {
        if (user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND).getItem() instanceof StaffItem
                && !user.isSneaking())
            return TypedActionResult.pass(user.getStackInHand(hand));
        return original.call(world, user, hand);
    }
}
