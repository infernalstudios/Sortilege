package net.lyof.sortilege.item.staff;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
        this.repairIngredient = () -> Ingredient.EMPTY;
        this.chargeTime = 1;
    }

    public StaffTier(Tier parent) {
        this();
        this.durability = parent.getUses();
        this.enchantability = parent.getEnchantmentValue();
        this.repairIngredient = parent::getRepairIngredient;
        this.attackDamage = parent.getAttackDamageBonus();
        this.fireproof = parent == Tiers.NETHERITE;
    }

    public static StaffTier read(JsonObject json) throws JsonSyntaxException {
        StaffTier self;
        try {
            self = new StaffTier(Tiers.valueOf(GsonHelper.getAsString(json, "parent", "")));
        } catch (IllegalArgumentException ignored) {
            self = new StaffTier();
        }

        if (json.has("fireproof"))
            self.fireproof = GsonHelper.getAsBoolean(json, "fireproof");
        if (json.has("durability"))
            self.durability = GsonHelper.getAsInt(json, "durability");
        if (json.has("repair_material")) {
            String id = GsonHelper.getAsString(json, "repair_material");
            if (id.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(id.substring(1)));
                self.repairIngredient = Suppliers.memoize(() -> Ingredient.of(tag));
            } else {
                self.repairIngredient = Suppliers.memoize(() -> Ingredient.of(BuiltInRegistries.ITEM.get(Identifier.of(id))));
            }
        }
        if (json.has("enchantability"))
            self.enchantability = GsonHelper.getAsInt(json, "enchantability");

        if (json.has("damage"))
            self.attackDamage = GsonHelper.getAsInt(json, "damage");
        if (json.has("piercing"))
            self.piercing = GsonHelper.getAsInt(json, "piercing");
        if (json.has("range"))
            self.range = GsonHelper.getAsInt(json, "range");
        if (json.has("charge_time"))
            self.chargeTime = GsonHelper.getAsInt(json, "charge_time");
        if (json.has("cooldown"))
            self.cooldown = GsonHelper.getAsInt(json, "cooldown");

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

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public boolean isFireproof() {
        return this.fireproof;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.attackDamage;
    }

    public int getPiercing() {
        return this.piercing;
    }

    public int getRange() {
        return this.range;
    }

    public int getChargeTime() {
        return this.chargeTime;
    }

    public int getCooldown() {
        return this.cooldown;
    }
}
