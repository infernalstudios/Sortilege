package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {
    @ModifyReturnValue(method = "craft", at = @At("RETURN"))
    public ItemStack enforceEnchantments(ItemStack original) {
        NbtCompound nbt = original.getNbt();
        if (nbt == null) return original;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(original);
        EnchantmentHelper.set(Map.of(), original);
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (entry.getKey().isAcceptableItem(original))
                original.addEnchantment(entry.getKey(), entry.getValue());
        }
        return original;
    }
}
