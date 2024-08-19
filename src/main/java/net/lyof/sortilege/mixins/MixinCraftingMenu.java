package net.lyof.sortilege.mixins;

import net.lyof.sortilege.configs.ConfigEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CraftingMenu.class)
public class MixinCraftingMenu {
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

    @Redirect(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setRecipeUsed(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/Recipe;)Z"))
    private static boolean lockCrafting(ResultContainer instance, Level world, ServerPlayer player, Recipe recipe) {
        String recipeid = recipe.getId().toString();
        int level = player.experienceLevel;
        Double required = ConfigEntries.xpRequirements.getOrDefault(recipeid, -1.0);

        if (level < required.intValue()) {
            player.sendSystemMessage(
                    Component.translatable("sortilege.requires_level", required.intValue())
                            .withStyle(ChatFormatting.YELLOW), true);
            world.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1, 1);
        }
        return level >= required.intValue() && instance.setRecipeUsed(world, player, recipe);
    }
}
