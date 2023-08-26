package net.lyof.sortilege.enchants.staff;

import net.lyof.sortilege.items.custom.StaffItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class StaffEnchantment extends Enchantment {
    public static EnchantmentCategory STAFF =
            EnchantmentCategory.create("STAFF", item -> item instanceof StaffItem || item == Items.ENCHANTED_BOOK);

    public int maxLevel;

    public StaffEnchantment(Rarity pRarity, int maxLevel) {
        super(pRarity, STAFF, EquipmentSlot.values());
        this.maxLevel = maxLevel;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }
}
