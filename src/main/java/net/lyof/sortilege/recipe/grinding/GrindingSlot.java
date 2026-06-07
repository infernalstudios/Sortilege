package net.lyof.sortilege.recipe.grinding;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GrindingSlot extends Slot {
    private final Slot parent;

    public GrindingSlot(Slot parent) {
        super(parent.container, parent.getContainerSlot(), parent.x, parent.y);
        this.parent = parent;
    }

    public boolean mayPlace(ItemStack stack) {
        return this.parent.mayPlace(stack) || (ConfigEntries.knowledgeEnabled && stack.is(ModItems.KNOWLEDGE_BOOK));
    }
}
