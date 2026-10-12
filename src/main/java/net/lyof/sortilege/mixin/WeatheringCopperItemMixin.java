package net.lyof.sortilege.mixin;

import com.teamabnormals.caverns_and_chasms.common.item.copper.WeatheringCopperItem;
import net.lcc.sollib.api.common.SolRegistries;
import net.lcc.sollib.api.common.registry.holder.ItemHolder;
import net.lyof.sortilege.item.staff.entry.WeatheringStaffReader;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WeatheringCopperItem.class)
public interface WeatheringCopperItemMixin {
    @Inject(method = "getWaxed", at = @At("HEAD"), cancellable = true)
    private static void getWaxed(Item item, CallbackInfoReturnable<Optional<Item>> cir) {
        WeatheringStaffReader.bootstrap();
        if (WeatheringStaffReader.WAX_ON_BY_ITEM.containsKey(item))
            cir.setReturnValue(Optional.ofNullable(WeatheringStaffReader.WAX_ON_BY_ITEM.get(item)));
    }

    @Inject(method = "getUnwaxed", at = @At("HEAD"), cancellable = true)
    private static void getUnwaxed(Item item, CallbackInfoReturnable<Optional<Item>> cir) {
        WeatheringStaffReader.bootstrap();
        if (WeatheringStaffReader.WAX_OFF_BY_ITEM.containsKey(item))
            cir.setReturnValue(Optional.ofNullable(WeatheringStaffReader.WAX_OFF_BY_ITEM.get(item)));
    }

    @Inject(method = "getNext(Lnet/minecraft/world/item/Item;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void getNext(Item item, CallbackInfoReturnable<Optional<Item>> cir) {
        WeatheringStaffReader.bootstrap();
        if (WeatheringStaffReader.NEXT_BY_ITEM.containsKey(item))
            cir.setReturnValue(Optional.ofNullable(WeatheringStaffReader.NEXT_BY_ITEM.get(item)));
    }

    @Inject(method = "getPrevious(Lnet/minecraft/world/item/Item;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void getPrevious(Item item, CallbackInfoReturnable<Optional<Item>> cir) {
        WeatheringStaffReader.bootstrap();
        if (WeatheringStaffReader.PREVIOUS_BY_ITEM.containsKey(item))
            cir.setReturnValue(Optional.ofNullable(WeatheringStaffReader.PREVIOUS_BY_ITEM.get(item)));
    }
}
