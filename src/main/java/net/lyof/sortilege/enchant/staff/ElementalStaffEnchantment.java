package net.lyof.sortilege.enchant.staff;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ElementalStaffEnchantment extends StaffEnchantment {
    public List<float[]> colors;

    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel) {
        this(pRarity, maxLevel, List.of(new float[]{1f, 1f, 1f}), null, null);
    }

    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel,
                                     List<float[]> colors, BiConsumer<LivingEntity, Integer> effect) {
        this(pRarity, maxLevel, colors, effect, null);
    }

    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel, List<float[]> colors,
                                     BiConsumer<LivingEntity, Integer> effect, Predicate<Enchantment> condition) {
        super(pRarity, maxLevel, effect, condition);
        this.colors = colors;
    }

    @Override
    protected boolean canAccept(Enchantment candidate) {
        return !(candidate instanceof ElementalStaffEnchantment) && super.canAccept(candidate);
    }
}
