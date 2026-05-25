package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneScreenHandlerMixin {
    @ModifyReturnValue(method = "grind", at = @At("RETURN"))
    private ItemStack grindLearnable(ItemStack original) {
        original.removeSubNbt(EnchantKnowledge.ITEM_KEY);
        return original;
    }
}
