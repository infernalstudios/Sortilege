package net.lyof.sortilege.mixins;

import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(UpgradeRecipe.class)
public class MixinUpgradeRecipe {
    @Shadow @Final Ingredient base;

    @Shadow @Final ItemStack result;

    @Shadow @Final Ingredient addition;

    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    public void addExtraEnchant(Container container, CallbackInfoReturnable<ItemStack> cir) {
        if (Arrays.stream(this.base.getItems()).anyMatch((stack) -> stack.is(this.result.getItem()))
                && Arrays.stream(this.addition.getItems()).anyMatch((stack) -> stack.is(ItemHelper.LIMIT_BREAKER))) {

            cir.setReturnValue(ItemHelper.addExtraEnchant(cir.getReturnValue()));
        }

        if (Arrays.stream(this.base.getItems()).anyMatch((stack) -> stack.is(this.result.getItem()))
                && Arrays.stream(this.addition.getItems()).anyMatch((stack) -> stack.is(ModTags.Items.SOULBINDERS))) {

            ItemStack result = cir.getReturnValue();
            if (!ItemHelper.hasEnchant(ModEnchants.SOULBOUND, result))
                result.enchant(ModEnchants.SOULBOUND.get(), 1);
            cir.setReturnValue(result);
        }
    }
}
