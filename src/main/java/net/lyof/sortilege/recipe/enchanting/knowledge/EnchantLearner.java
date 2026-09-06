package net.lyof.sortilege.recipe.enchanting.knowledge;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

public interface EnchantLearner {
    EnchantKnowledge sorti_getKnowledge();

    @Environment(EnvType.CLIENT)
    class Cache {
        public static ItemStack stack = null;
        public static EnchantKnowledge knowledge = null;

        public static void update(@Nullable ItemStack stack, EnchantLearner learner) {
            Cache.stack = stack;
            Cache.knowledge = stack == null ? null : learner.sorti_getKnowledge();
        }

        public static boolean isLearnable(Holder<Enchantment> enchantment) {
            return knowledge != null && knowledge.isLearnable(stack, enchantment, EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack));
        }
    }
}
