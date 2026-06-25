package net.lyof.sortilege.mixin;

import com.teamabnormals.caverns_and_chasms.common.item.copper.WeatheringCopperItem;
import net.lyof.sortilege.item.ModItems;
import net.lyof.sortilege.item.custom.staff.WeatheringExperienceStaffItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WeatheringCopperItem.class)
public interface WeatheringCopperItemMixin {
    @Inject(method = "getWaxed", at = @At("HEAD"), cancellable = true)
    private static void getWaxed(ItemStack stack, CallbackInfoReturnable<Optional<ItemStack>> cir) {
        if (WeatheringExperienceStaffItem.WAX_ON_BY_ITEM.containsKey(stack.getItem()))
            cir.setReturnValue(Optional.ofNullable(WeatheringExperienceStaffItem.WAX_ON_BY_ITEM.get(stack.getItem())).map(ItemStack::new));
    }

    @Inject(method = "getUnwaxed", at = @At("HEAD"), cancellable = true)
    private static void getUnwaxed(ItemStack stack, CallbackInfoReturnable<Optional<ItemStack>> cir) {
        if (WeatheringExperienceStaffItem.WAX_OFF_BY_ITEM.containsKey(stack.getItem()))
            cir.setReturnValue(Optional.ofNullable(WeatheringExperienceStaffItem.WAX_OFF_BY_ITEM.get(stack.getItem())).map(ItemStack::new));
    }

    @Inject(method = "getNext(Lnet/minecraft/world/item/Item;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void getNext(Item item, CallbackInfoReturnable<Optional<Item>> cir) {
        if (WeatheringExperienceStaffItem.NEXT_BY_ITEM.containsKey(item))
            cir.setReturnValue(Optional.ofNullable(WeatheringExperienceStaffItem.NEXT_BY_ITEM.get(item)));
    }

    @Inject(method = "getPrevious(Lnet/minecraft/world/item/Item;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void getPrevious(Item item, CallbackInfoReturnable<Optional<Item>> cir) {
        if (WeatheringExperienceStaffItem.PREVIOUS_BY_ITEM.containsKey(item))
            cir.setReturnValue(Optional.ofNullable(WeatheringExperienceStaffItem.PREVIOUS_BY_ITEM.get(item)));
    }
}
