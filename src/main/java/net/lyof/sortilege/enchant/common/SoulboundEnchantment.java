package net.lyof.sortilege.enchant.common;

import net.lyof.sortilege.setup.ModTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SoulboundEnchantment extends Enchantment {
    public SoulboundEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.VANISHABLE, EquipmentSlot.values());
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getMaxStackSize() == 1 && !stack.isEdible() && !stack.is(ModTags.Items.SOULBIND_BLACKLIST)
                && !(stack.getItem() instanceof BucketItem) && !(stack.getItem() instanceof BlockItem);
    }
}
