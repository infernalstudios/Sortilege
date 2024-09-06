package net.lyof.sortilege.mixin;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.setup.ModTags;
import net.lyof.sortilege.util.ItemHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(method = "getAllOfType", at = @At("RETURN"), cancellable = true)
    public <C extends Inventory, T extends Recipe<C>> void addLimiteRecipes(RecipeType<T> type, CallbackInfoReturnable<Map<Identifier, T>> cir) {
        Map<Identifier, T> map = cir.getReturnValue();
        if (type != RecipeType.SMITHING)
            return;

        Map<Identifier, SmithingRecipe> recipes = new HashMap<>();
        for (Identifier key : map.keySet())
            recipes.put(key, (SmithingRecipe) map.get(key));

        Identifier id;
        for (Item item : ItemHelper.ENCHANTABLES) {
            id = Sortilege.makeID(Registries.ITEM.getId(item).getPath() + "_limit_break");

            recipes.put(id, new SmithingTransformRecipe(
                    id,
                    Ingredient.ofItems(Items.LAPIS_LAZULI),
                    Ingredient.ofItems(item),
                    Ingredient.fromTag(ModTags.Items.LIMIT_BREAKER),
                    new ItemStack(item)));
        }

        for (Item item : ItemHelper.SOULBINDABLES) {
            id = Sortilege.makeID(Registries.ITEM.getId(item).getPath() + "_soulbind");

            recipes.put(id, new SmithingTransformRecipe(
                    id,
                    Ingredient.ofItems(Items.LAPIS_LAZULI),
                    Ingredient.ofItems(item),
                    Ingredient.fromTag(ModTags.Items.SOULBINDERS),
                    new ItemStack(item)));
        }

        cir.setReturnValue((Map<Identifier, T>) recipes);
    }
}
