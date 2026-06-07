package net.lyof.sortilege.enchant.staff;

import com.chocohead.mm.api.ClassTinkerers;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.StaffItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class StaffEnchantment extends Enchantment {
    private static final EnchantmentCategory STAFF = ClassTinkerers.getEnum(EnchantmentCategory.class, "Sortilege$STAFF");
    private static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

    private final int maxLevel;
    private final BiConsumer<LivingEntity, Integer> effect;
    private final Predicate<Enchantment> condition;

    public StaffEnchantment(Rarity rarity, int maxLevel) {
        this(rarity, maxLevel, null, null);
    }

    public StaffEnchantment(Rarity rarity, int maxLevel,
                            BiConsumer<LivingEntity, Integer> effect, Predicate<Enchantment> condition) {
        super(rarity, STAFF, SLOTS);
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

    @Override
    public boolean canEnchant(ItemStack stack) {
        return super.canEnchant(stack) && (this != ModEnchants.WISDOM
                || (stack.getItem() instanceof StaffItem staff && staff.getXPCost(stack) > 0));
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMaxCost(level) * 5;
    }
}
