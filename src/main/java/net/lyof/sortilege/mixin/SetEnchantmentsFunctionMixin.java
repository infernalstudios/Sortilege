package net.lyof.sortilege.mixin;

import net.lyof.sortilege.item.ModDataComponents;
import net.lyof.sortilege.setup.ModConfig;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SetEnchantmentsFunction.class)
public class SetEnchantmentsFunctionMixin {
    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;updateEnchantments(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;"))
    private void makeLearnable(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        if (ModConfig.knowledgeEnabled.get())
            stack.set(ModDataComponents.LEARNABLE, Unit.INSTANCE);
    }
}
