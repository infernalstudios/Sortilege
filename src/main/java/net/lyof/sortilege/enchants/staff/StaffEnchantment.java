package net.lyof.sortilege.enchants.staff;

import net.lyof.sortilege.items.custom.StaffItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class StaffEnchantment extends Enchantment {
    public static EnchantmentCategory STAFF =
            EnchantmentCategory.create("STAFF", item -> item instanceof StaffItem || item == Items.ENCHANTED_BOOK);

    public int maxLevel;
    public BiConsumer<LivingEntity, Integer> effect;
    public Predicate<Enchantment> condition;

    public StaffEnchantment(Rarity pRarity, int maxLevel) {
        this(pRarity, maxLevel, null, null);
    }

    public StaffEnchantment(Rarity pRarity, int maxLevel,
                            BiConsumer<LivingEntity, Integer> effect, Predicate<Enchantment> condition) {
        super(pRarity, STAFF, EquipmentSlot.values());
        this.maxLevel = maxLevel;
        this.effect = effect;
        this.condition = condition;
    }

    @Override
    protected boolean checkCompatibility(Enchantment candidate) {
        return (this.condition == null || this.condition.test(candidate)) && super.checkCompatibility(candidate);
    }

    public void triggerAttack(LivingEntity target, int level) {
        if (this.effect != null)
            this.effect.accept(target, level);
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }
}
