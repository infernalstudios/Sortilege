package net.lyof.sortilege.item.custom.staff;

import com.binaris.wizardry.api.content.util.CastItemDataHelper;
import com.binaris.wizardry.setup.registries.EBItems;
import com.cleannrooster.rpgmana.Rpgmana;
import com.cleannrooster.rpgmana.api.ManaInstance;
import com.cleannrooster.rpgmana.api.ManaInterface;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonObject;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.attribute.ModAttributes;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.spell_engine.SpellEngineMod;
import net.spell_engine.api.enchantment.Enchantments_SpellEngine;
import net.spell_engine.client.gui.SpellTooltip;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import net.spell_power.api.enchantment.SpellPowerEnchanting;
import net.spell_power.internals.CustomEntityAttribute;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class RPGManaStaffItem extends AStaffItem {
    @Dependency(mod = "rpgmana:mana")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new Cost().read(json);
        }

        @Override
        public StaffEntry.Effects readEffects(JsonObject json) {
            return new Effects().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new RPGManaStaffItem(entry, new Properties());
        }
    }

    protected static class Cost extends StaffEntry.Cost {
        protected Supplier<Ingredient> items;
        protected Supplier<Component> itemsTranslation;
        protected int mana;

        @Override
        public StaffEntry.Cost read(JsonObject json) {
            super.read(json);

            String items = GsonHelper.getAsString(json, "items", "");
            if (items.isEmpty()) {
                this.itemsTranslation = null;
                this.items = () -> Ingredient.EMPTY;
            } else if (items.startsWith("#")) {
                ResourceLocation id = Identifier.of(items.substring(1));
                TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
                this.itemsTranslation = Suppliers.memoize(() -> Component.translatable("tag.item." + id.getNamespace() + "." + id.getPath()));
                this.items = Suppliers.memoize(() -> Ingredient.of(tag));
            } else {
                ResourceLocation id = Identifier.of(items);
                this.itemsTranslation = Suppliers.memoize(() -> BuiltInRegistries.ITEM.get(id).getDescription());
                this.items = Suppliers.memoize(() -> Ingredient.of(BuiltInRegistries.ITEM.get(id)));
            }

            this.mana = GsonHelper.getAsInt(json, "mana", 10);
            return this;
        }

        public int getMana() {
            return this.mana;
        }

        public Ingredient getItems() {
            return items.get();
        }

        public Component getItemsTranslation() {
            return this.itemsTranslation == null ? null : ((MutableComponent) this.itemsTranslation.get()).withStyle(ChatFormatting.GRAY);
        }
    }

    public static class Effects extends StaffEntry.Effects {
        protected String school;

        @Override
        public StaffEntry.Effects read(JsonObject json) {
            super.read(json);
            this.school = GsonHelper.getAsString(json, "school", "spell_power:arcane");
            return this;
        }

        public SpellSchool getSchool() {
            SpellSchool school = SpellSchools.getSchool(this.school);
            return school == null ? SpellSchools.ARCANE : school;
        }
    }


    protected final Cost cost;
    protected final Effects effects;

    public RPGManaStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (Cost) this.getEntry().getCost();
        this.effects = (Effects) this.getEntry().getEffects();
    }
/*
    @Override
    public void addAttributeModifiers(ItemStack stack, ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        builder.put(effects.getSchool().attribute, new AttributeModifier(((CustomEntityAttribute) effects.getSchool().attribute).nameUUID,
                "School modifier", effects.getSchoolPower(), AttributeModifier.Operation.ADDITION));
    }*/

    @Override
    public float modifyDamageDealt(ItemStack stack, float damage, LivingEntity player, LivingEntity target, Set<ElementalStaffEnchantment> elements) {
        float m = 0;
        if (player instanceof ManaInterface mana) {
            if (mana.getMana() > 0.1) {
                for (ManaInstance instance : mana.getManaInstances())
                    m += (float) instance.value;
            }
        }


        player.getAttributes().addTransientAttributeModifiers(ImmutableMultimap.of(
                effects.getSchool().attribute, new AttributeModifier(((CustomEntityAttribute) effects.getSchool().attribute).nameUUID,
                "School modifier", 1, AttributeModifier.Operation.ADDITION))
        );
        SpellPower.Result spellPower = SpellPower.getSpellPower(effects.getSchool(), player);
        float s = (float) spellPower.randomValue(SpellPower.getVulnerability(target, effects.getSchool()));

        return super.modifyDamageDealt(stack, damage, player, target, elements) * (1 + m * 0.01f / 4) * s;
    }

    @Override
    public int getCost(ItemStack stack, Player player, int original) {
        int o = super.getCost(stack, player, original);
        double m = 1 + Rpgmana.config.inspiration * 0.01 * SpellPowerEnchanting.getEnchantmentLevel(Rpgmana.ARCHMAGE, player, null)
                - Rpgmana.config.manastabilized * 0.01 * SpellPowerEnchanting.getEnchantmentLevel(Rpgmana.MANASTABILIZED, player, null)
                + player.getAttributeValue(Rpgmana.MANACOST) * 0.001;
        return (int) (m * o);
    }

    @Override
    public int getCooldown(ItemStack stack, Player player) {
        float multiplier = SpellPower.getHaste(player, effects.getSchool());
        return (int) (super.getCooldown(stack, player) / multiplier);
    }

    public boolean hasItem(ItemStack stack, Player player) {
        if (cost.getItems().test(Items.ARROW.getDefaultInstance())) {
            if (EnchantHelper.hasEnchant(Enchantments.INFINITY_ARROWS, stack))
                return true;
        }
        else if (EnchantHelper.hasEnchant(Enchantments_SpellEngine.INFINITY, stack))
            return true;

        return player.getInventory().hasAnyMatching(cost.getItems());
    }

    public boolean hasMana(ItemStack stack, Player player) {
        return player instanceof ManaInterface mana && mana.getMana() > 0.1;
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return this.hasItem(stack, player) || this.hasMana(stack, player);
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        if (cost.getItems().test(Items.ARROW.getDefaultInstance())) {
            if (EnchantHelper.hasEnchant(Enchantments.INFINITY_ARROWS, stack))
                return;
        }
        else if (EnchantHelper.hasEnchant(Enchantments_SpellEngine.INFINITY, stack))
            return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (this.cost.getItems().test(s) && s != stack) {
                s.shrink(1);
                return;
            }
        }

        if (player instanceof ManaInterface mana)
            mana.spendMana(-this.getCost(stack, player, cost.getMana()));
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (cost.getItemsTranslation() != null) {
            boolean hasAmmo = this.hasItem(stack, player);
            tooltip.add(Component.translatable("sortilege.staff.cost.item.spell_engine", cost.getItemsTranslation())
                    .withStyle(hasAmmo ? ChatFormatting.GREEN : ChatFormatting.RED));
        } if (this.getCost(stack, player, cost.getMana()) > 0) {
            tooltip.add(Component.translatable("rpgmana.manacost")
                    .append(Component.literal(": " + this.getCost(stack, player, cost.getMana())))
                    .withStyle(ChatFormatting.BLUE));
        }
    }
}
