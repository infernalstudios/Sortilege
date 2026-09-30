package net.lyof.sortilege.item.staff;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.lcc.sollib.core.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

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

    public void copy(Tier parent) {
        this.durability = parent.getUses();
        this.enchantability = parent.getEnchantmentValue();
        this.repairIngredient = parent::getRepairIngredient;
        this.attackDamage = parent.getAttackDamageBonus();
        this.fireproof = parent == Tiers.NETHERITE;
    }

    public StaffTier read(JsonObject json) throws JsonSyntaxException {
        try {
            this.copy(Tiers.valueOf(GsonHelper.getAsString(json, "parent", "")));
        } catch (IllegalArgumentException ignored) {}

        if (json.has("fireproof"))
            this.fireproof = GsonHelper.getAsBoolean(json, "fireproof");
        if (json.has("durability"))
            this.durability = GsonHelper.getAsInt(json, "durability");
        if (json.has("repair_material")) {
            String id = GsonHelper.getAsString(json, "repair_material");
            if (id.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.of(id.substring(1)));
                this.repairIngredient = Suppliers.memoize(() -> Ingredient.of(tag));
            } else {
                this.repairIngredient = Suppliers.memoize(() -> Ingredient.of(BuiltInRegistries.ITEM.get(Identifier.of(id))));
            }
        }
        if (json.has("enchantability"))
            this.enchantability = GsonHelper.getAsInt(json, "enchantability");

        if (json.has("damage"))
            this.attackDamage = GsonHelper.getAsFloat(json, "damage");
        if (json.has("piercing"))
            this.piercing = GsonHelper.getAsInt(json, "piercing");
        if (json.has("range"))
            this.range = GsonHelper.getAsInt(json, "range");
        if (json.has("charge_time"))
            this.chargeTime = GsonHelper.getAsInt(json, "charge_time");
        if (json.has("cooldown"))
            this.cooldown = GsonHelper.getAsInt(json, "cooldown");

        return this;
    }

    @Override
    public float getSpeed() {
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

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
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
