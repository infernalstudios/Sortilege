package net.lyof.sortilege.mixin;

import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {
    @Shadow @Final
    Ingredient base;

    @Shadow @Final
    ItemStack result;

    @Shadow @Final
    Ingredient addition;

    @Inject(method = "craft", at = @At("RETURN"), cancellable = true)
    public void addExtraEnchant(Inventory inventory, DynamicRegistryManager registryManager, CallbackInfoReturnable<ItemStack> cir) {
        if (this.base.test(this.result.getItem().getDefaultStack()) &&
                this.addition.test(ItemHelper.LIMIT_BREAKER.getDefaultStack())) {

            cir.setReturnValue(ItemHelper.addExtraEnchant(cir.getReturnValue()));
        }

        if (this.base.test(this.result.getItem().getDefaultStack()) &&
                Arrays.stream(this.addition.getMatchingStacks()).anyMatch(stack -> stack.isIn(ModTags.Items.SOULBINDERS))) {

            ItemStack result = cir.getReturnValue();
            if (!ItemHelper.hasEnchant(ModEnchants.SOULBOUND, result))
                result.addEnchantment(ModEnchants.SOULBOUND, 1);
            cir.setReturnValue(result);
        }
    }
}
