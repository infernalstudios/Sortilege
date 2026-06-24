package net.lyof.sortilege.item.custom.staff;

import com.binaris.wizardry.api.content.item.ICustomDamageItem;
import com.binaris.wizardry.api.content.item.IManaItem;
import com.binaris.wizardry.api.content.item.IWorkbenchItem;
import com.binaris.wizardry.api.content.util.CastItemDataHelper;
import com.binaris.wizardry.api.content.util.DrawingUtils;
import com.binaris.wizardry.api.content.util.WorkbenchUtils;
import com.binaris.wizardry.core.config.EBServerConfig;
import com.binaris.wizardry.setup.registries.EBAdvancementTriggers;
import com.binaris.wizardry.setup.registries.EBItems;
import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ElectroblobStaffItem extends AStaffItem implements IManaItem, IWorkbenchItem, ICustomDamageItem {
    @Dependency(mod = "ebwizardry:mana")
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
            return new ElectroblobStaffItem(entry, new Item.Properties());
        }
    }

    public static class Effects extends StaffEntry.Effects {
        protected int maxUpgrades;
        protected int range;
        protected float cooldown;
        protected int siphon;
        protected int condenser;
        protected float storage;

        @Override
        public Effects read(JsonObject json) {
            super.read(json);
            JsonObject upgrades = GsonHelper.getAsJsonObject(json, "upgrades", new JsonObject());
            this.maxUpgrades = GsonHelper.getAsInt(upgrades, "max", 1);
            this.range = GsonHelper.getAsInt(upgrades, "range_per_level", 4);
            this.cooldown = GsonHelper.getAsFloat(upgrades, "cooldown_per_level", 0.1f);
            this.siphon = GsonHelper.getAsInt(upgrades, "siphon_per_level", 5);
            this.condenser = GsonHelper.getAsInt(upgrades, "condenser_interval", 50);
            this.storage = GsonHelper.getAsFloat(upgrades, "storage_per_level", 0.15f);
            return this;
        }

        public int getMaxUpgrades() {
            return this.maxUpgrades;
        }

        public int getRange() {
            return this.range;
        }

        public float getCooldown() {
            return this.cooldown;
        }

        public int getSiphon() {
            return this.siphon;
        }

        public int getCondenser() {
            return this.condenser;
        }

        public float getStorage() {
            return this.storage;
        }
    }


    protected final ValueCost cost;
    protected final Effects effects;

    public ElectroblobStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (ValueCost) this.getEntry().getCost();
        this.effects = (Effects) this.getEntry().getEffects();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return this.getMana(stack) >= this.getCost(stack, player, cost.getValue());
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        this.consumeMana(stack, this.getCost(stack, player, cost.getValue() - 1), player);
    }

    @Override
    public boolean canMelee(ItemStack stack) {
        return super.canMelee(stack) ||
                (CastItemDataHelper.getUpgradeLevel(stack, EBItems.MELEE_UPGRADE.get()) > 0 && this.canShoot(stack, null));
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getValue()) > 0)
            tooltip.add(Component.translatable("tooltip.sortilege.staff.cost.mana", this.getCost(stack, player, cost.getValue())).withStyle(ChatFormatting.BLUE));
    }

    @Override
    public int getRange(ItemStack stack) {
        return super.getRange(stack) + CastItemDataHelper.getUpgradeLevel(stack, EBItems.RANGE_UPGRADE.get()) * this.effects.getRange();
    }

    @Override
    public int getCooldown(ItemStack stack, Player player) {
        float multiplier = 1 - CastItemDataHelper.getUpgradeLevel(stack, EBItems.COOLDOWN_UPGRADE.get()) * this.effects.getCooldown();
        return (int) (super.getCooldown(stack, player) * Math.max(0, multiplier));
    }

    @Override
    public double getBlastRadius(ItemStack stack, LivingEntity player) {
        return super.getBlastRadius(stack, player) + CastItemDataHelper.getUpgradeLevel(stack, EBItems.BLAST_UPGRADE.get());
    }

    @Override
    public void onKill(ItemStack stack, LivingEntity player, LivingEntity target) {
        super.onKill(stack, player, target);
        this.rechargeMana(stack, this.effects.getSiphon() * CastItemDataHelper.getUpgradeLevel(stack, EBItems.SIPHON_UPGRADE.get()));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity player) {
        if (player instanceof Player &&
                (this.getOvercharge(stack) <= 0 || !this.getEntry().getCost().getOvercharge().ignoresCost()))
            this.consumeResource(stack, (Player) player);
        return super.hurtEnemy(stack, target, player);
    }

    @Override
    public int getMana(ItemStack stack) {
        return this.getManaCapacity(stack) - stack.getDamageValue();
    }

    @Override
    public int getManaCapacity(ItemStack stack) {
        return stack.getMaxDamage();
    }

    @Override
    public void setMana(ItemStack stack, int mana) {
        stack.setDamageValue(this.getManaCapacity(stack) - mana);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
        if (!world.isClientSide() && !this.isManaFull(stack) && world.getGameTime() % this.effects.getCondenser() == 0)
            this.rechargeMana(stack, CastItemDataHelper.getUpgradeLevel(stack, EBItems.CONDENSER_UPGRADE.get()));
    }

    @Override
    public int getCustomMaxDamage(ItemStack stack) {
        return (int) (this.getMaxDamage() * (1 + this.effects.getStorage()
                * CastItemDataHelper.getUpgradeLevel(stack, EBItems.STORAGE_UPGRADE.get())) + 0.5);
    }

    @Override
    public void setCustomDamage(ItemStack stack, int damage) {
        stack.getOrCreateTag().putInt("Damage", Math.max(0, Math.min(damage, stack.getMaxDamage())));
    }

    @Override
    public boolean canBreak(ItemStack stack) {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return DrawingUtils.mix(16747518, 9318116, (float) stack.getDamageValue());
    }

    @Override
    public int getSpellSlotCount(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean onApplyButtonPressed(Player player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks) {
        boolean changed = false;
        if (upgrade.hasItem())
            changed = this.applyUpgradeSlot(player, centre, upgrade);

        changed |= WorkbenchUtils.rechargeManaFromCrystals(centre, crystals);
        return changed;
    }

    protected boolean applyUpgradeSlot(Player player, Slot centre, Slot upgrade) {
        ItemStack original = centre.getItem().copy();
        centre.set(this.applyUpgrade(player, centre.getItem(), upgrade.getItem()));
        return !ItemStack.isSameItem(centre.getItem(), original);
    }

    @Override
    public ItemStack applyUpgrade(Player player, ItemStack stack, ItemStack upgrade) {
        Item specialUpgrade = upgrade.getItem();

        if (!isValidUpgrade(specialUpgrade)) return stack;

        if (CastItemDataHelper.getTotalUpgrades(stack) < this.effects.getMaxUpgrades()
                && CastItemDataHelper.getUpgradeLevel(stack, specialUpgrade) < EBServerConfig.UPGRADE_STACK_LIMIT.get()) {

            int prevMana = this.getMana(stack);
            CastItemDataHelper.applyUpgrade(stack, specialUpgrade);
            if (specialUpgrade == EBItems.STORAGE_UPGRADE.get())
                this.setMana(stack, prevMana);

            upgrade.shrink(1);
            if (player != null)
                EBAdvancementTriggers.SPECIAL_UPGRADE.triggerFor(player);
        }
        return stack;
    }

    protected boolean isValidUpgrade(Item upgrade) {
        return upgrade == EBItems.STORAGE_UPGRADE.get()  //
                || upgrade == EBItems.CONDENSER_UPGRADE.get()  //
                || upgrade == EBItems.BLAST_UPGRADE.get()  //
                || upgrade == EBItems.COOLDOWN_UPGRADE.get()  //
                || upgrade == EBItems.MELEE_UPGRADE.get()  //
                || upgrade == EBItems.RANGE_UPGRADE.get()  //
                || upgrade == EBItems.SIPHON_UPGRADE.get();  //
    }

    @Override
    public boolean showTooltip(ItemStack stack) {
        return true;
    }
}
