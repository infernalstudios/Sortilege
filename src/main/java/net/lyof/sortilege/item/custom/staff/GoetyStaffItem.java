package net.lyof.sortilege.item.custom.staff;

import com.Polarice3.Goety.api.items.IPersist;
import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.events.spell.GoetyEventFactory;
import com.Polarice3.Goety.config.ItemConfig;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.TotemFinder;
import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.EnchantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.function.BiConsumer;

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
        public void register(StaffEntry entry, BiConsumer<String, AStaffItem> registrar) {
            registrar.accept(entry.getID(), new GoetyStaffItem(entry, new Item.Properties().customDamage(GoetyStaffItem::damageItem)));
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

    protected static final ResourceLocation BRAZIER = Sortilege.MOD.makeID("brazier");
    protected static final ResourceLocation BLIZZARD = Sortilege.MOD.makeID("blizzard");
    protected static final ResourceLocation BLAST = Sortilege.MOD.makeID("blast");
    protected static final ResourceLocation BLITZ = Sortilege.MOD.makeID("blitz");
    protected static final ResourceLocation BLESSING = Sortilege.MOD.makeID("blessing");

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
            public SpellType getSpellType() { return GoetyStaffItem.this.getSpellType(stack, player); }

            @Override
            public List<ResourceKey<Enchantment>> acceptedEnchantments() { return List.of(); }
        };
    }

    public SpellType getSpellType(ItemStack stack, Player player) {
        SpellType base = effects.getSpellType();
        if (base != SpellType.NONE) return base;

        Registry<Enchantment> registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        if (registry.getHolder(BRAZIER).isPresent() && EnchantmentHelper.getItemEnchantmentLevel(registry.getHolder(BRAZIER).get(), stack) > 0)
            return SpellType.NETHER;
        if (registry.getHolder(BLIZZARD).isPresent() && EnchantmentHelper.getItemEnchantmentLevel(registry.getHolder(BLIZZARD).get(), stack) > 0)
            return SpellType.FROST;
        if (registry.getHolder(BLAST).isPresent() && EnchantmentHelper.getItemEnchantmentLevel(registry.getHolder(BLAST).get(), stack) > 0)
            return SpellType.GEOMANCY;
        if (registry.getHolder(BLITZ).isPresent() && EnchantmentHelper.getItemEnchantmentLevel(registry.getHolder(BLITZ).get(), stack) > 0)
            return SpellType.STORM;
        if (registry.getHolder(BLESSING).isPresent() && EnchantmentHelper.getItemEnchantmentLevel(registry.getHolder(BLESSING).get(), stack) > 0)
            return SpellType.NECROMANCY;

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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (effects.persists() && this.isBroken(stack))
            tooltip.add(Component.translatable("info.goety.armor.broken").withStyle(ChatFormatting.DARK_RED));
        else super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public void appendTooltipAbilities(ItemStack stack, Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable("info.goety.focus.spellType", this.getSpellType(stack, player).getName()));

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
    public boolean canMelee(ItemStack stack) {
        return !isBroken(stack) && super.canMelee(stack);
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
    public boolean shouldDisplayAttributes(ItemStack stack, Player player) {
        return !this.isBroken(stack) && super.shouldDisplayAttributes(stack, player);
    }

    private static int damageItem(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot, Runnable onBroken) {
        if (stack.getItem() instanceof GoetyStaffItem item && item.effects.persists()) {
            if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
                if (stack.getDamageValue() != stack.getMaxDamage() - 1) {
                    stack.setDamageValue(stack.getMaxDamage() - 1);
                    onBroken.run();
                }
                return 0;
            }
        }
        return amount;
    }
}
