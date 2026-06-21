package net.lyof.sortilege.item.custom.staff;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.events.spell.GoetyEventFactory;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.TotemFinder;
import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

// Goety code scares me
public class GoetyStaffItem extends AStaffItem {
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
            return new GoetyStaffItem(entry, new Properties());
        }
    }

    public static class Effects extends StaffEntry.Effects {
        protected SpellType spellType;

        @Override
        public StaffEntry.Effects read(JsonObject json) {
            super.read(json);
            try {
                this.spellType = SpellType.valueOf(GsonHelper.getAsString(json, "spell_type", "NONE"));
            } catch (Exception e) {
                this.spellType = SpellType.NONE;
            }
            return this;
        }

        public SpellType getSpellType() {
            return this.spellType;
        }
    }

    protected final ValueCost cost;
    protected final Effects effects;

    public GoetyStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (ValueCost) this.getEntry().getCost();
        this.effects = (Effects) this.getEntry().getEffects();
    }

    @Override
    public int getCost(ItemStack stack, Player player, int original) {
        return (int) (new ISpell() {
            @Override
            public int defaultSoulCost() { return GoetyStaffItem.super.getCost(stack, player, cost.getValue()); }

            @Override
            public int defaultCastDuration() { return 0; }

            @Override
            public int defaultSpellCooldown() { return 0; }

            @Override
            public SpellType getSpellType() { return GoetyStaffItem.this.effects.getSpellType(); }

            @Override
            public List<Enchantment> acceptedEnchantments() { return List.of(); }
        }.soulCost(player, stack) * SEHelper.soulDiscount(player));
    }

    public boolean hasResource(ItemStack stack, Player player, int cost) {
        return SEHelper.getSoulAmountInt(player) >= cost;
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return this.hasResource(stack, player, this.getCost(stack, player, cost.getValue()));
    }

    public void consumeResource(ItemStack stack, Player player, int cost) {
        cost = GoetyEventFactory.onSoulEnergyLoss(player, cost);
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
    public void consumeResource(ItemStack stack, Player player) {
        this.consumeResource(stack, player, this.getCost(stack, player, this.cost.getValue()));
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getValue()) > 0)
            tooltip.add(Component.translatable("info.goety.wand.cost", this.getCost(stack, player, cost.getValue())));
    }
}
