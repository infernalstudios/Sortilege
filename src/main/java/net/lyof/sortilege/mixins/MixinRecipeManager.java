package net.lyof.sortilege.mixins;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.utils.ItemHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(RecipeManager.class)
public class MixinRecipeManager {
    @Inject(method = "byType", at = @At("RETURN"), cancellable = true)
    public <C extends Container, T extends Recipe<C>> void addLimiteRecipes(RecipeType<T> type,
                                                                            CallbackInfoReturnable<Map<ResourceLocation, T>> cir) {
        Map<ResourceLocation, T> map = cir.getReturnValue();
        if (type != RecipeType.SMITHING)
            return;

        Map<ResourceLocation, UpgradeRecipe> recipes = new HashMap<>();
        for (ResourceLocation key : map.keySet())
            recipes.put(key, (UpgradeRecipe) map.get(key));

        ResourceLocation id;
        for (Item item : ItemHelper.ENCHANTABLES) {
            id = new ResourceLocation(Sortilege.MOD_ID, Registry.ITEM.getKey(item).getPath() + "_limit_break");

            recipes.put(id, new UpgradeRecipe(
                    id,
                    Ingredient.of(item),
                    Ingredient.of(ModTags.Items.LIMIT_BREAKER),
                    new ItemStack(item)));
        }

        for (Item item : ItemHelper.SOULBINDABLES) {
            id = new ResourceLocation(Sortilege.MOD_ID, Registry.ITEM.getKey(item).getPath() + "_soulbind");

            recipes.put(id, new UpgradeRecipe(
                    id,
                    Ingredient.of(item),
                    Ingredient.of(ModTags.Items.SOULBINDERS),
                    new ItemStack(item)));
        }

        cir.setReturnValue((Map<ResourceLocation, T>) recipes);
    }
}
