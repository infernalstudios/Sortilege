package net.lyof.sortilege.items.armor;

import net.lyof.sortilege.Sortilege;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {
    WITCH("witch_hat", 94, new int[]{1, 2, 3, 1}, 24, SoundEvents.WITCH_CELEBRATE,
            0.0F, 0.0F, () -> Ingredient.of(Items.PHANTOM_MEMBRANE));

    private static final int[] HEALTH_PER_SLOT = new int[]{1, 1, 1, 1};
    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durability, int[] defense, int enchantability, SoundEvent sound, float toughness, float knockbackRes, Supplier<Ingredient> ingredient) {
        this.name = name;
        this.durabilityMultiplier = durability;
        this.slotProtections = defense;
        this.enchantmentValue = enchantability;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackRes;
        this.repairIngredient = new LazyLoadedValue<>(ingredient);
    }

    public int getDurabilityForSlot(EquipmentSlot slot) {
        return HEALTH_PER_SLOT[slot.getIndex()] * this.durabilityMultiplier;
    }

    public int getDefenseForSlot(EquipmentSlot slot) {
        return this.slotProtections[slot.getIndex()];
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public SoundEvent getEquipSound() {
        return this.sound;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return Sortilege.MOD_ID + ":" + this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}