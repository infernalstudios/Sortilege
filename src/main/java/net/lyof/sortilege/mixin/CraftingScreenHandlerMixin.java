package net.lyof.sortilege.mixin;

import net.lyof.sortilege.crafting.RecipeLock;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;shouldCraftRecipe(Lnet/minecraft/world/World;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/Recipe;)Z"))
    private static boolean lockCrafting(CraftingResultInventory instance, World world, ServerPlayerEntity player, Recipe recipe) {
        String recipeid = recipe.getId().toString();
        RecipeLock lock = RecipeLock.get(recipeid);
        boolean valid = true;

        if (lock.matches(player)) {
            valid = false;
            player.sendMessage(
                    lock.getFailMessage(player)
                            .formatted(Formatting.YELLOW), true);
        }
        return valid && instance.shouldCraftRecipe(world, player, recipe);
    }
}
