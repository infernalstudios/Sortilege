package net.lyof.sortilege.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lyof.sortilege.recipe.crafting.RecipeLock;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
    @WrapOperation(method = "slotChangedCraftingGrid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setRecipeUsed(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)Z"))
    private static boolean lockCrafting(ResultContainer instance, Level level, ServerPlayer player, RecipeHolder<CraftingRecipe> recipe, Operation<Boolean> original) {
        String recipeid = recipe.id().toString();
        RecipeLock lock = RecipeLock.get(recipeid);
        boolean valid = true;

        if (lock.matches(player)) {
            valid = false;
            player.displayClientMessage(lock.getFailMessage(player).withStyle(ChatFormatting.YELLOW), true);
        }
        return valid && original.call(instance, level, player, recipe);
    }
}
