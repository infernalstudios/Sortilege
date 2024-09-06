package net.lyof.sortilege.mixin;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {
    @Shadow @Final
    Ingredient base;

    @Shadow @Final
    ItemStack result;

    @Shadow @Final
    Ingredient addition;

    @Shadow @Final private Identifier id;

    @Inject(method = "craft", at = @At("RETURN"), cancellable = true)
    public void addExtraEnchant(Inventory inventory, DynamicRegistryManager registryManager, CallbackInfoReturnable<ItemStack> cir) {
        if (this.id.toString().endsWith("_limit_break")) {
            cir.setReturnValue(ItemHelper.addExtraEnchant(cir.getReturnValue()));
        }

        if (this.id.toString().endsWith("_soulbind")) {
            ItemStack result = cir.getReturnValue();
            if (!ItemHelper.hasEnchant(ModEnchants.SOULBOUND, result))
                result.addEnchantment(ModEnchants.SOULBOUND, 1);
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    public void stopUseless(Inventory inventory, World world, CallbackInfoReturnable<Boolean> cir) {
        if (this.id.toString().endsWith("_limit_break")) {
            if (ItemHelper.getExtraEnchants(inventory.getStack(1)) >= ConfigEntries.maxLimitBreak) cir.setReturnValue(false);
        }

        if (this.id.toString().endsWith("_soulbind")) {
            if (ItemHelper.hasEnchant(ModEnchants.SOULBOUND, inventory.getStack(1))) cir.setReturnValue(false);
        }
    }
}
