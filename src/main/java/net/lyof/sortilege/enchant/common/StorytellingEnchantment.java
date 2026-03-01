package net.lyof.sortilege.enchant.common;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StorytellingEnchantment extends Enchantment {
    public StorytellingEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public Text getName(int level) {
        MutableText text = (MutableText) super.getName(level);
        if (ConfigEntries.altStorytelling) text = text.formatted(Formatting.DARK_AQUA);
        return text;
    }
}
