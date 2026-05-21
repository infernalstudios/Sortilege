package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantRandomlyLootFunction.class)
public class EnchantRandomlyLootFunctionMixin {
    @Inject(method = "addEnchantmentToStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;addEnchantment(Lnet/minecraft/enchantment/Enchantment;I)V"))
    private static void makeLearnable(ItemStack stack, Enchantment enchantment, Random random, CallbackInfoReturnable<ItemStack> cir) {
        if (Sortilege.log(ConfigEntries.enableKnowledge))
            stack.getOrCreateNbt().putBoolean(EnchantKnowledge.ITEM_KEY, true);
    }
}
