package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {
    @ModifyReturnValue(method = "assemble", at = @At("RETURN"))
    public ItemStack enforceEnchantments(ItemStack original) {
        CompoundTag nbt = original.getTag();
        if (nbt == null) return original;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(original);
        EnchantmentHelper.setEnchantments(Map.of(), original);
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (entry.getKey().canEnchant(original))
                original.enchant(entry.getKey(), entry.getValue());
        }
        return original;
    }
}
