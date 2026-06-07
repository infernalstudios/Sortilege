package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.recipe.enchanting.knowledge.EnchantKnowledge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SetEnchantmentsFunction.class)
public class SetEnchantmentsFunctionMixin {
    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;setEnchantments(Ljava/util/Map;Lnet/minecraft/world/item/ItemStack;)V"))
    private void makeLearnable(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        if (ConfigEntries.knowledgeEnabled)
            stack.getOrCreateTag().putBoolean(EnchantKnowledge.LEARNABLE_KEY, true);
    }
}
