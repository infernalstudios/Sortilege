package net.lyof.sortilege.mixins;

import net.lyof.sortilege.configs.ConfigEntries;
import net.lyof.sortilege.enchants.ModEnchants;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(UpgradeRecipe.class)
public abstract class MixinUpgradeRecipe {
    @Shadow @Final Ingredient base;

    @Shadow @Final ItemStack result;

    @Shadow @Final Ingredient addition;

    @Shadow public abstract ResourceLocation getId();

    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    public void addExtraEnchant(Container container, CallbackInfoReturnable<ItemStack> cir) {
        if (this.getId().toString().endsWith("_limit_break")) {
            cir.setReturnValue(ItemHelper.addExtraEnchant(cir.getReturnValue()));
        }

        if (this.getId().toString().endsWith("_soulbind")) {
            ItemStack result = cir.getReturnValue();
            if (!ItemHelper.hasEnchant(ModEnchants.SOULBOUND, result))
                result.enchant(ModEnchants.SOULBOUND.get(), 1);
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    public void stopUseless(Container inventory, Level world, CallbackInfoReturnable<Boolean> cir) {
        if (this.getId().toString().endsWith("_limit_break")) {
            if (ItemHelper.getExtraEnchants(inventory.getItem(1)) >= ConfigEntries.maxLimitBreak) cir.setReturnValue(false);
        }

        if (this.getId().toString().endsWith("_soulbind")) {
            if (ItemHelper.hasEnchant(ModEnchants.SOULBOUND, inventory.getItem(1))) cir.setReturnValue(false);
        }
    }
}
