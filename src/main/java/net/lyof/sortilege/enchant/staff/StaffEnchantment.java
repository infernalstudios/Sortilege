package net.lyof.sortilege.enchant.staff;

import com.chocohead.mm.api.ClassTinkerers;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.StaffItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class StaffEnchantment extends Enchantment {
    private static final EnchantmentTarget STAFF = ClassTinkerers.getEnum(EnchantmentTarget.class, "Sortilege$STAFF");
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
    protected boolean canAccept(Enchantment candidate) {
        return (this.condition == null || this.condition.test(candidate)) && super.canAccept(candidate);
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
    public boolean isAcceptableItem(ItemStack stack) {
        return super.isAcceptableItem(stack) && (this != ModEnchants.WISDOM
                || (stack.getItem() instanceof StaffItem staff && staff.getXPCost(stack) > 0));
    }

    @Override
    public int getMaxPower(int level) {
        return super.getMaxPower(level) * 5;
    }
}
