package net.lyof.sortilege.recipe.enchanting.knowledge;

import net.minecraft.world.item.ItemStack;

public interface EnchantLearner {
    EnchantKnowledge sorti_getKnowledge(ItemStack cacher);
}
