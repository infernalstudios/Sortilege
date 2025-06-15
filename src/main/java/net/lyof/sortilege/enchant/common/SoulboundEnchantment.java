package net.lyof.sortilege.enchant.common;

import net.lyof.sortilege.setup.ModTags;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;

public class SoulboundEnchantment extends Enchantment {
    public SoulboundEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.VANISHABLE, EquipmentSlot.values());
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getMaxCount() == 1 && !stack.isFood() && !stack.isIn(ModTags.Items.SOULBIND_BLACKLIST)
                && !(stack.getItem() instanceof BucketItem) && !(stack.getItem() instanceof BlockItem);
    }
}
