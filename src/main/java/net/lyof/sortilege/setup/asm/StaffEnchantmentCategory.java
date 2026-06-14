package net.lyof.sortilege.setup.asm;

import net.lyof.sortilege.item.custom.AStaffItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

public class StaffEnchantmentCategory extends EnchantmentCategoryMixin {
    @Override
    public boolean canEnchant(Item item) {
        return item instanceof AStaffItem;
    }
}

@Mixin(EnchantmentCategory.class)
abstract class EnchantmentCategoryMixin {
    @Shadow abstract boolean canEnchant(Item other);
}