package net.lyof.sortilege.mixin;

import net.lyof.sortilege.configs.ConfigEntries;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
    /**
     * @author Lyof (Sortilege)
     * @reason Locking recipes behind xp levels
     *//*
    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private static void slotChangedCraftingGrid(AbstractContainerMenu menu, Level world, Player player,
                                                  CraftingContainer craftingContainer, ResultContainer resultContainer, CallbackInfo ci) {
        if (!world.isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer)player;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingContainer, world);
            if (optional.isPresent()) {
                CraftingRecipe craftingrecipe = optional.get();

                // Here's the part I actually change
                String recipeid = craftingrecipe.getId().toString();
                int level = serverplayer.experienceLevel;
                Double required = ConfigEntries.xpRequirements.getOrDefault(recipeid, -1.0);

                if (level >= required.intValue()) {
                    if (resultContainer.setRecipeUsed(world, serverplayer, craftingrecipe)) {
                        itemstack = craftingrecipe.assemble(craftingContainer);
                    }
                }
                else {
                    serverplayer.sendSystemMessage(
                            Component.translatable("sortilege.requires_level").append(" " + required.intValue() + "!")
                                    .withStyle(ChatFormatting.YELLOW), true);
                    world.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1, 1);
                }
                //
            }

            resultContainer.setItem(0, itemstack);
            menu.setRemoteSlot(0, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, itemstack));

            ci.cancel();
        }
    }*/

    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;shouldCraftRecipe(Lnet/minecraft/world/World;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/Recipe;)Z"))
    private static boolean lockCrafting(CraftingResultInventory instance, World world, ServerPlayerEntity player, Recipe recipe) {
        String recipeid = recipe.getId().toString();
        int level = player.experienceLevel;
        Double required = ConfigEntries.xpRequirements.getOrDefault(recipeid, -1.0);

        if (level < required.intValue()) {
            player.sendMessage(
                    Text.translatable("sortilege.requires_level").append(" " + required.intValue() + "!")
                            .formatted(Formatting.YELLOW), true);
            world.playSound(player, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 1, 1);
        }
        return level >= required.intValue() && instance.shouldCraftRecipe(world, player, recipe);
    }
}
