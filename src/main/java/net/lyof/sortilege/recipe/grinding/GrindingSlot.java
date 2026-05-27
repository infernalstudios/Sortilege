package net.lyof.sortilege.recipe.grinding;

import net.lyof.sortilege.config.ConfigEntries;
import net.lyof.sortilege.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class GrindingSlot extends Slot {
    private final Slot parent;

    public GrindingSlot(Slot parent) {
        super(parent.inventory, parent.getIndex(), parent.x, parent.y);
        this.parent = parent;
    }

    public boolean canInsert(ItemStack stack) {
        return this.parent.canInsert(stack) || (ConfigEntries.knowledgeEnabled && stack.isOf(ModItems.KNOWLEDGE_BOOK));
    }
}
