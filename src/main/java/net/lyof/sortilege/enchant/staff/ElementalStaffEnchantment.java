package net.lyof.sortilege.enchant.staff;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ElementalStaffEnchantment extends StaffEnchantment {
    public List<Triple<Float, Float, Float>> colors;

    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel) {
        this(pRarity, maxLevel, List.of(new MutableTriple<>(1f, 1f, 1f)), null, null);
    }

    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel,
                                     List<Triple<Float, Float, Float>> colors, BiConsumer<LivingEntity, Integer> effect) {
        this(pRarity, maxLevel, colors, effect, null);
    }

    public ElementalStaffEnchantment(Rarity pRarity, int maxLevel, List<Triple<Float, Float, Float>> colors,
                                     BiConsumer<LivingEntity, Integer> effect, Predicate<Enchantment> condition) {
        super(pRarity, maxLevel, effect, condition);
        this.colors = colors;
    }

    @Override
    protected boolean canAccept(Enchantment candidate) {
        return !(candidate instanceof ElementalStaffEnchantment) && super.canAccept(candidate);
    }
}
