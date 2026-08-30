package net.lyof.sortilege.mixin;

// TODO @Mixin(WeatheringCopperItem.class)
public interface WeatheringCopperItemMixin {/*
    @Inject(method = "getWaxed", at = @At("HEAD"), cancellable = true)
    private static void getWaxed(ItemStack stack, CallbackInfoReturnable<Optional<ItemStack>> cir) {
        if (WeatheringStaffReader.WAX_ON_BY_ITEM.containsKey(stack.getItem()))
            cir.setReturnValue(Optional.ofNullable(WeatheringStaffReader.WAX_ON_BY_ITEM.get(stack.getItem())).map(ItemStack::new));
    }

    @Inject(method = "getUnwaxed", at = @At("HEAD"), cancellable = true)
    private static void getUnwaxed(ItemStack stack, CallbackInfoReturnable<Optional<ItemStack>> cir) {
        if (WeatheringStaffReader.WAX_OFF_BY_ITEM.containsKey(stack.getItem()))
            cir.setReturnValue(Optional.ofNullable(WeatheringStaffReader.WAX_OFF_BY_ITEM.get(stack.getItem())).map(ItemStack::new));
    }

    @Inject(method = "getNext(Lnet/minecraft/world/item/Item;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void getNext(Item item, CallbackInfoReturnable<Optional<Item>> cir) {
        if (WeatheringStaffReader.NEXT_BY_ITEM.containsKey(item))
            cir.setReturnValue(Optional.ofNullable(WeatheringStaffReader.NEXT_BY_ITEM.get(item)));
    }

    @Inject(method = "getPrevious(Lnet/minecraft/world/item/Item;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void getPrevious(Item item, CallbackInfoReturnable<Optional<Item>> cir) {
        if (WeatheringStaffReader.PREVIOUS_BY_ITEM.containsKey(item))
            cir.setReturnValue(Optional.ofNullable(WeatheringStaffReader.PREVIOUS_BY_ITEM.get(item)));
    }*/
}
