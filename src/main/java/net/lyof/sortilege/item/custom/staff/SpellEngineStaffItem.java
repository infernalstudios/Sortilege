package net.lyof.sortilege.item.custom.staff;

import com.cleannrooster.rpgmana.Rpgmana;
import com.cleannrooster.rpgmana.api.ManaInstance;
import com.cleannrooster.rpgmana.api.ManaInterface;
import com.cleannrooster.rpgmana.internals.ManaCosts;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonObject;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.MathHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.spell_engine.SpellEngineMod;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;
import net.spell_power.internals.CustomEntityAttribute;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SpellEngineStaffItem extends AStaffItem {
    @Dependency(mod = "spell_engine:rune")
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
        public void register(StaffEntry entry, BiConsumer<String, AStaffItem> registrar) {
            registrar.accept(entry.getID(), new SpellEngineStaffItem(entry, new Properties()));
        }
    }

    protected static class Cost extends StaffEntry.Cost {
        protected Supplier<Ingredient> rune;
        protected Supplier<Component> runeTranslation;
        protected int mana;
        protected boolean manaEnabled;

        @Override
        public StaffEntry.Cost read(JsonObject json) {
            super.read(json);

            String items = GsonHelper.getAsString(json, "rune", "");
            if (items.isEmpty()) {
                this.runeTranslation = null;
                this.rune = () -> Ingredient.EMPTY;
            } else if (items.startsWith("#")) {
                ResourceLocation id = Identifier.of(items.substring(1));
                TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
                this.runeTranslation = Suppliers.memoize(() -> Component.translatable("tag.item." + id.getNamespace() + "." + id.getPath()));
                this.rune = Suppliers.memoize(() -> Ingredient.of(tag));
            } else {
                ResourceLocation id = Identifier.of(items);
                this.runeTranslation = Suppliers.memoize(() -> BuiltInRegistries.ITEM.get(id).getDescription());
                this.rune = Suppliers.memoize(() -> Ingredient.of(BuiltInRegistries.ITEM.get(id)));
            }

            this.mana = GsonHelper.getAsInt(json, "mana", 10);
            try {
                Class.forName("com.cleannrooster.rpgmana.api.ManaInterface");
                this.manaEnabled = this.mana > 0;
            } catch (ClassNotFoundException e) {
                this.manaEnabled = false;
            }

            return this;
        }

        public Ingredient getRune() {
            return rune.get();
        }

        public Component getRuneTranslation() {
            return this.runeTranslation == null ? null : ((MutableComponent) this.runeTranslation.get()).withStyle(ChatFormatting.GRAY);
        }

        public int getMana() {
            return this.mana;
        }

        public boolean isManaEnabled() {
            return this.manaEnabled;
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


    public static final ResourceLocation SPELL_INFINITY = Identifier.of("spell_engine", "spell_infinity");
    public static final ResourceLocation SCHOOL_ATTRIBUTE = Sortilege.MOD.makeID("staff.spell_engine_school");

    protected final Cost cost;
    protected final Effects effects;

    public SpellEngineStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (Cost) this.getEntry().getCost();
        this.effects = (Effects) this.getEntry().getEffects();
    }

    @Override
    public float modifyDamageDealt(ItemStack stack, float damage, LivingEntity player, LivingEntity target, DamageSource source) {
        float m = 0;
        if (cost.isManaEnabled()) {
            try {
                if (player instanceof ManaInterface mana) {
                    if (mana.getMana() > 0.1) {
                        for (ManaInstance instance : mana.getManaInstances())
                            m += (float) instance.value;
                    }
                }
            } catch (Throwable ignored) {}
        }

        if (effects.getSchool().ownsAttribute())
            player.getAttributes().addTransientAttributeModifiers(ImmutableMultimap.of(
                    effects.getSchool().getAttributeEntry(),
                    new AttributeModifier(SCHOOL_ATTRIBUTE, 1, AttributeModifier.Operation.ADD_VALUE)
            ));
        SpellPower.Result spellPower = SpellPower.getSpellPower(effects.getSchool(), player);
        float s = (float) spellPower.randomValue(SpellPower.getVulnerability(target, effects.getSchool()));

        return super.modifyDamageDealt(stack, damage, player, target, source) * (1 + m * 0.01f / 4) * s;
    }

    @Override
    public int getCost(ItemStack stack, Player player, int original) {
        int o = super.getCost(stack, player, original);

        if (cost.isManaEnabled()) {
            try {
                return (int) (o * player.getAttributeValue(Rpgmana.MANA_ATTRIBUTE) * 0.01);
            } catch (Throwable ignored) {}
        }

        return o;
    }

    @Override
    public int getCooldown(ItemStack stack, Player player) {
        float multiplier = SpellPower.getHaste(player, effects.getSchool());
        return (int) (super.getCooldown(stack, player) / multiplier);
    }

    public boolean hasItem(ItemStack stack, Player player) {
        /*Registry<Enchantment> registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        if (cost.getRune().test(Items.ARROW.getDefaultInstance())) {
            if (EnchantmentHelper.getItemEnchantmentLevel(registry.getHolder(Enchantments.INFINITY).get(), stack) > 0)
                return true;
        }
        else if (EnchantmentHelper.getItemEnchantmentLevel(registry.getHolder(SPELL_INFINITY).get(), stack) > 0)
            return true;*/

        for (ItemStack ammo : this.cost.getRune().getItems()) {
            int c = player.level() instanceof ServerLevel server
                    ? EnchantmentHelper.processAmmoUse(server, stack, ammo, this.getCost(stack, player, 1))
                    : this.getCost(stack, player, 1);

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.is(ammo.getItem()) && s != stack) {
                    int k = Math.min(c, s.getCount());
                    s.shrink(k);
                    c -= k;
                }
                if (c <= 0) return true;
            }
        }
        return false;
    }

    public boolean hasMana(ItemStack stack, Player player) {
        if (cost.isManaEnabled()) {
            try {
                return ManaCosts.canAfford(player, this.getCost(stack, player, cost.getMana()));
            } catch (Throwable ignored) {}
        }
        return false;
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return this.hasItem(stack, player) || this.hasMana(stack, player);
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        int count = -1;
        ItemStack ammo = ItemStack.EMPTY;
        for (ItemStack a : this.cost.getRune().getItems()) {
            int c = player.level() instanceof ServerLevel server
                    ? EnchantmentHelper.processAmmoUse(server, stack, a, this.getCost(stack, player, 1))
                    : this.getCost(stack, player, 1);
            int total = c;
            if (count >= 0 && total > count) continue;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.is(a.getItem()) && s != stack) {
                    int k = Math.min(c, s.getCount());
                    s.shrink(k);
                    c -= k;
                }
                if (c <= 0) {
                    count = total;
                    ammo = a;
                };
            }
        }

        if (!ammo.isEmpty()) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.is(ammo.getItem()) && s != stack) {
                    int k = Math.min(count, s.getCount());
                    s.shrink(k);
                    count -= k;
                }
                if (count <= 0) return;
            }
        }

        if (cost.isManaEnabled()) {
            try {
                if (player instanceof ManaInterface mana)
                    mana.spendMana(-this.getCost(stack, player, cost.getMana()));
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (cost.getRuneTranslation() != null) {
            boolean hasAmmo = this.hasItem(stack, player);
            tooltip.add(Component.translatable("tooltip.sortilege.staff.cost.item.spell_engine", cost.getRuneTranslation())
                    .withStyle(hasAmmo ? ChatFormatting.GREEN : ChatFormatting.RED));
        } if (cost.isManaEnabled() && this.getCost(stack, player, cost.getMana()) > 0) {
            tooltip.add(Component.translatable("rpgmana.manacost")
                    .append(Component.literal(": " + this.getCost(stack, player, cost.getMana())))
                    .withStyle(ChatFormatting.BLUE));
        }
    }
}
