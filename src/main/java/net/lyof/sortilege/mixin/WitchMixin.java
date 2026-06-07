package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Witch.class)
public class WitchMixin {
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int getNonModifiedUseTime(ItemStack instance, Operation<Integer> original) {
        if (instance.is(Items.POTION))
            instance = Items.POTION.getDefaultInstance();
        return original.call(instance);
    }
}
