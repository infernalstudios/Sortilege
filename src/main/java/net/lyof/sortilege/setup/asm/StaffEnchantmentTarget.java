package net.lyof.sortilege.setup.asm;

import net.lyof.sortilege.item.custom.StaffItem;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

public class StaffEnchantmentTarget extends TargetEnchantmentMixin {
    @Override
    public boolean isAcceptableItem(Item item) {
        return item instanceof StaffItem;
    }
}

@Mixin(EnchantmentTarget.class)
abstract class TargetEnchantmentMixin {
    @Shadow
    abstract boolean isAcceptableItem(Item other);
}