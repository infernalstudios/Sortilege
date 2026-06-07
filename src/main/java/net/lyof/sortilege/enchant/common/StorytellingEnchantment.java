package net.lyof.sortilege.enchant.common;

import net.lyof.sortilege.config.ConfigEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class StorytellingEnchantment extends Enchantment {
    public StorytellingEnchantment(Rarity weight, EnchantmentCategory target, EquipmentSlot[] slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public Component getFullname(int level) {
        MutableComponent text = (MutableComponent) super.getFullname(level);
        if (ConfigEntries.altStorytelling) text = text.withStyle(ChatFormatting.DARK_AQUA);
        return text;
    }
}
