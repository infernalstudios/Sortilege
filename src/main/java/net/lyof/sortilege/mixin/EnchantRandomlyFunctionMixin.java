package net.lyof.sortilege.mixin;

import net.lyof.sortilege.setup.ModConfig;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantRandomlyFunction.class)
public class EnchantRandomlyFunctionMixin {
    @Inject(method = "enchantItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;enchant(Lnet/minecraft/world/item/enchantment/Enchantment;I)V"))
    private static void makeLearnable(ItemStack stack, Enchantment enchantment, RandomSource random, CallbackInfoReturnable<ItemStack> cir) {
        if (ModConfig.knowledgeEnabled.get())
            stack.getOrCreateTag().putBoolean(EnchantKnowledge.LEARNABLE_KEY, true);
    }
}
