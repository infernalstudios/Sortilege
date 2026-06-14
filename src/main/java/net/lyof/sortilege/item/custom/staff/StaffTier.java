package net.lyof.sortilege.item.custom.staff;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.lcc.sollib.api.common.registry.Holder;
import net.lcc.sollib.core.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public class StaffTier implements Tier {
    private int durability;
    private int enchantability;
    private Supplier<Ingredient> repairIngredient;
    private boolean fireproof;

    private float attackDamage;
    private int piercing;
    private int range;
    private int chargeTime;
    private int cooldown;

    public StaffTier() {
        this.setRepairIngredient(() -> Ingredient.EMPTY);
        this.setChargeTime(1);
    }

    public StaffTier(Tier parent) {
        this();
        this.setDurability(parent.getUses());
        this.setEnchantability(parent.getEnchantmentValue());
        this.setRepairIngredient(parent::getRepairIngredient);
        this.setAttackDamage(parent.getAttackDamageBonus());
        this.setFireproof(parent == Tiers.NETHERITE);
    }

    public static StaffTier read(JsonObject json) throws JsonSyntaxException {
        StaffTier self;
        try {
            self = new StaffTier(Tiers.valueOf(GsonHelper.getAsString(json, "parent", "")));
        } catch (IllegalArgumentException ignored) {
            self = new StaffTier();
        }

        if (json.has("fireproof"))
            self.setFireproof(GsonHelper.getAsBoolean(json, "fireproof"));
        if (json.has("durability"))
            self.setDurability(GsonHelper.getAsInt(json, "durability"));
        if (json.has("repair_material")) {
            String id = GsonHelper.getAsString(json, "repair_material");
            if (id.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(id.substring(1)));
                self.setRepairIngredient(Suppliers.memoize(() -> Ingredient.of(tag)));
            } else {
                self.setRepairIngredient(Suppliers.memoize(() -> Ingredient.of(BuiltInRegistries.ITEM.get(Identifier.of(id)))));
            }
        }
        if (json.has("enchantability"))
            self.setEnchantability(GsonHelper.getAsInt(json, "enchantability"));

        if (json.has("damage"))
            self.setAttackDamage(GsonHelper.getAsInt(json, "damage"));
        if (json.has("piercing"))
            self.setPiercing(GsonHelper.getAsInt(json, "piercing"));
        if (json.has("range"))
            self.setRange(GsonHelper.getAsInt(json, "range"));
        if (json.has("charge_time"))
            self.setChargeTime(GsonHelper.getAsInt(json, "charge_time"));
        if (json.has("cooldown"))
            self.setCooldown(GsonHelper.getAsInt(json, "cooldown"));

        return self;
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getUses() {
        return this.durability;
    }

    protected void setDurability(int durability) {
        this.durability = durability;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    protected void setEnchantability(int enchantability) {
        this.enchantability = enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    protected void setRepairIngredient(Supplier<Ingredient> repairIngredient) {
        this.repairIngredient = repairIngredient;
    }

    public boolean isFireproof() {
        return this.fireproof;
    }

    protected void setFireproof(boolean fireproof) {
        this.fireproof = fireproof;
    }


    @Override
    public float getAttackDamageBonus() {
        return this.attackDamage;
    }

    protected void setAttackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
    }

    public int getPiercing() {
        return this.piercing;
    }

    protected void setPiercing(int piercing) {
        this.piercing = piercing;
    }

    public int getRange() {
        return this.range;
    }

    protected void setRange(int range) {
        this.range = range;
    }

    public int getChargeTime() {
        return this.chargeTime;
    }

    protected void setChargeTime(int chargeTime) {
        this.chargeTime = chargeTime;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    protected void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}
