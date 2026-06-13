package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.item.custom.staff.StaffItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
public class ShieldItemMixin {
    @WrapMethod(method = "use")
    public InteractionResultHolder<ItemStack> handleStaffUse(Level world, Player user, InteractionHand hand,
                                                       Operation<InteractionResultHolder<ItemStack>> original) {
        if (user.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND).getItem() instanceof StaffItem
                && !user.isShiftKeyDown())
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        return original.call(world, user, hand);
    }
}
