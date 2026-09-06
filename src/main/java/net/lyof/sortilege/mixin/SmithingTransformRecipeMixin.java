package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {
    @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    public ItemStack enforceEnchantments(ItemStack original) {
        /*ItemEnchantments enchants = original.getEnchantments();
        EnchantmentHelper.setEnchantments(original, enchants);*/
        return original;
    }
}
