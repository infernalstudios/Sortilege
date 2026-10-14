package net.lyof.sortilege.item.custom.staff;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.lcc.sollib.core.Identifier;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.XPHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class IronsManaStaffItem extends AStaffItem {
    @Dependency(mod = "irons_spellbooks:mana")
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
        public void register(StaffEntry entry, BiConsumer<String, Supplier<AStaffItem>> registrar) {
            registrar.accept(entry.getID(), () -> new IronsManaStaffItem(entry, new Properties()));
        }
    }

    protected static class Cost extends StaffEntry.Cost {
        protected int mana;

        @Override
        public StaffEntry.Cost read(JsonObject json) {
            super.read(json);
            this.mana = GsonHelper.getAsInt(json, "mana");
            return this;
        }

        public int getMana() {
            return this.mana;
        }
    }

    public static class Effects extends StaffEntry.Effects {
        protected SchoolType school;

        @Override
        public StaffEntry.Effects read(JsonObject json) {
            super.read(json);
            this.school = SchoolRegistry.getSchool(Identifier.of(GsonHelper.getAsString(json, "school", "sortilege:none")));
            return this;
        }

        public SchoolType getSchool() {
            return this.school;
        }
    }

    protected final Cost cost;
    protected final Effects effects;

    public IronsManaStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (Cost) this.getEntry().getCost();
        this.effects = (Effects) this.getEntry().getEffects();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return MagicData.getPlayerMagicData(player).getMana() >= this.getCost(stack, player, cost.getMana());
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        MagicData.getPlayerMagicData(player).addMana(-this.getCost(stack, player, cost.getMana()));
    }

    @Override
    public int getCooldown(ItemStack stack, Player player) {
        float m = (float) player.getAttributeValue(AttributeRegistry.COOLDOWN_REDUCTION);
        return (int) (super.getCooldown(stack, player) * m);
    }

    @Override
    public float getStaffDamage(ItemStack stack, LivingEntity player) {
        float m = (float) player.getAttributeValue(AttributeRegistry.SPELL_POWER);
        float s = effects.getSchool() == null ? (float) effects.getSchool().getPowerFor(player) : 1;
        return super.getStaffDamage(stack, player) * m * s;
    }

    @Override
    public float modifyDamageDealt(ItemStack stack, float damage, LivingEntity player, LivingEntity target, DamageSource source) {
        float s = effects.getSchool() == null ? (float) effects.getSchool().getResistanceFor(target) : 1;
        return super.modifyDamageDealt(stack, damage, player, target, source) / s;
        // TODO: change damage type
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getMana()) > 0)
            tooltip.add(Component.translatable("tooltip.irons_spellbooks.mana_cost", this.getCost(stack, player, cost.getMana())).withStyle(ChatFormatting.BLUE));
    }

    @Override
    public void appendTooltipAbilities(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipAbilities(stack, player, tooltip);

        if (effects.getSchool() != null)
            tooltip.add(effects.getSchool().getDisplayName());
    }
}
