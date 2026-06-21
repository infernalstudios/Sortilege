package net.lyof.sortilege.item.custom.staff;

import com.Polarice3.Goety.api.items.IPersist;
import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.events.spell.GoetyEventFactory;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.ItemConfig;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.TotemFinder;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

// Goety code scares me
public class GoetyStaffItem extends AStaffItem implements IPersist {
    @Dependency(mod = "goety:soul_energy")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new ValueCost().read(json);
        }

        @Override
        public StaffEntry.Effects readEffects(JsonObject json) {
            return new Effects().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new GoetyStaffItem(entry, new FabricItemSettings().customDamage(GoetyStaffItem::damageItem));
        }
    }

    public static class Effects extends StaffEntry.Effects {
        protected SpellType spellType = SpellType.NONE;
        protected boolean persists;

        @Override
        public StaffEntry.Effects read(JsonObject json) {
            super.read(json);
            String spellType = GsonHelper.getAsString(json, "spell_type", "NONE").toLowerCase();
            for (SpellType type : SpellType.values())
                if (type.getBaseName().equals(spellType))
                    this.spellType = type;
            this.persists = GsonHelper.getAsBoolean(json, "persists", false);
            return this;
        }

        public SpellType getSpellType() {
            return this.spellType;
        }

        public boolean persists() {
            return this.persists;
        }
    }

    protected final ValueCost cost;
    protected final Effects effects;

    public GoetyStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (ValueCost) this.getEntry().getCost();
        this.effects = (Effects) this.getEntry().getEffects();
    }

    public ISpell getSpell(ItemStack stack, Player player) {
        return new ISpell() {
            @Override
            public int defaultSoulCost() { return GoetyStaffItem.super.getCost(stack, player, cost.getValue()); }

            @Override
            public int defaultCastDuration() { return 0; }

            @Override
            public int defaultSpellCooldown() { return 0; }

            @Override
            public SpellType getSpellType() { return GoetyStaffItem.this.getSpellType(stack); }

            @Override
            public List<Enchantment> acceptedEnchantments() { return List.of(); }
        };
    }

    public SpellType getSpellType(ItemStack stack) {
        SpellType base = effects.getSpellType();
        if (base != SpellType.NONE) return base;

        if (EnchantHelper.hasEnchant(ModEnchants.BRAZIER, stack)) return SpellType.NETHER;
        if (EnchantHelper.hasEnchant(ModEnchants.BLIZZARD, stack)) return SpellType.FROST;
        if (EnchantHelper.hasEnchant(ModEnchants.BLAST, stack)) return SpellType.GEOMANCY;
        if (EnchantHelper.hasEnchant(ModEnchants.BLITZ, stack)) return SpellType.STORM;
        if (EnchantHelper.hasEnchant(ModEnchants.BLESSING, stack)) return SpellType.NECROMANCY;
        return base;
    }

    @Override
    public int getCost(ItemStack stack, Player player, int original) {
        return (int) (this.getSpell(stack, player).soulCost(player, stack) * SEHelper.soulDiscount(player));
    }

    @Override
    public int getCooldown(ItemStack stack, Player player) {
        if (this.getSpell(stack, player).ReduceCastTime(player))
            return super.getCooldown(stack, player) / 2;
        return super.getCooldown(stack, player);
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return SEHelper.getSoulAmountInt(player) >= this.getCost(stack, player, cost.getValue());
    }

    public void consumeResource(ItemStack stack, Player player) {
        int cost = GoetyEventFactory.onSoulEnergyLoss(player, this.getCost(stack, player, this.cost.getValue()));
        if (SEHelper.getSEActive(player)) {
            SEHelper.decreaseSESouls(player, cost);
            SEHelper.sendSEUpdatePacket(player);
        } else {
            ItemStack foundStack = TotemFinder.FindTotem(player);
            if (foundStack != null)
                ITotem.decreaseSouls(foundStack, cost);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        if (effects.persists() && this.isBroken(stack))
            tooltip.add(Component.translatable("info.goety.armor.broken").withStyle(ChatFormatting.DARK_RED));
        else super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public void appendTooltipAbilities(ItemStack stack, Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable("info.goety.focus.spellType", this.getSpellType(stack).getName()));

        super.appendTooltipAbilities(stack, player, tooltip);
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getValue()) > 0)
            tooltip.add(Component.translatable("info.goety.wand.cost", this.getCost(stack, player, cost.getValue())));
    }

    @Override
    public boolean isBroken(ItemStack stack) {
        return IPersist.super.isBroken(stack) && effects.persists();
    }

    @Override
    public boolean canShoot(ItemStack stack, Player player) {
        return !isBroken(stack) && super.canShoot(stack, player);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (this.isBroken(stack))
            return 0x800000;
        return super.getBarColor(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (this.isBroken(stack))
            return 13;
        return super.getBarWidth(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        return this.isBroken(stack) ? ImmutableMultimap.of() : super.getAttributeModifiers(stack, slot);
    }

    private static <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        if (stack.getItem() instanceof GoetyStaffItem item && item.effects.persists()) {
            if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
                if (stack.getDamageValue() != stack.getMaxDamage() - 1) {
                    stack.setDamageValue(stack.getMaxDamage() - 1);
                    onBroken.accept(entity);
                }
                return 0;
            }
        }
        return amount;
    }
}
