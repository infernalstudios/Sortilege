package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SetEnchantmentsLootFunction.class)
public class SetEnchantmentsLootFunctionMixin {
    @Inject(method = "process", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;set(Ljava/util/Map;Lnet/minecraft/item/ItemStack;)V"))
    private void makeLearnable(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        if (ConfigEntries.enableKnowledge)
            stack.getOrCreateNbt().putBoolean(EnchantKnowledge.ITEM_KEY, true);
    }
}
