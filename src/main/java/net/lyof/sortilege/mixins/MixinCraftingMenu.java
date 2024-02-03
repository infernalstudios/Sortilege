package net.lyof.sortilege.mixins;

import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.configs.ConfigEntries;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(CraftingMenu.class)
public class MixinCraftingMenu {
    /**
     * @author Lyof (Sortilege)
     * @reason Locking recipes behind xp levels
     */
    @Overwrite
    protected static void slotChangedCraftingGrid(AbstractContainerMenu menu, Level world, Player player, 
                                                  CraftingContainer craftingContainer, ResultContainer resultContainer) {
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

                if (level >= required.intValue() && resultContainer.setRecipeUsed(world, serverplayer, craftingrecipe)) {
                    itemstack = craftingrecipe.assemble(craftingContainer);
                }
                //
            }

            resultContainer.setItem(0, itemstack);
            menu.setRemoteSlot(0, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, itemstack));
        }
    }
}
