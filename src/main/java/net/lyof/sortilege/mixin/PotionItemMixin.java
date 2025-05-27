package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @WrapMethod(method = "getMaxUseTime")
    public int getConfiguredUseTime(ItemStack stack, Operation<Integer> original) {
        return ConfigEntries.potionDrinkingTime <= 0 ? original.call(stack) : ConfigEntries.potionDrinkingTime;
    }
}
