package net.lyof.sortilege.item.custom.staff;

import com.binaris.wizardry.api.content.item.ICustomDamageItem;
import com.binaris.wizardry.api.content.item.IManaItem;
import com.binaris.wizardry.api.content.item.IWorkbenchItem;
import com.binaris.wizardry.api.content.spell.SpellContext;
import com.binaris.wizardry.api.content.util.CastItemDataHelper;
import com.binaris.wizardry.api.content.util.DrawingUtils;
import com.binaris.wizardry.api.content.util.WorkbenchUtils;
import com.binaris.wizardry.content.item.WandItem;
import com.binaris.wizardry.core.config.EBServerConfig;
import com.binaris.wizardry.setup.registries.EBAdvancementTriggers;
import com.binaris.wizardry.setup.registries.EBItems;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.Sortilege;
import net.lyof.sortilege.enchant.ModEnchants;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.lyof.sortilege.util.EnchantHelper;
import net.lyof.sortilege.util.XPHelper;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WizardryManaStaffItem extends AStaffItem implements IManaItem, IWorkbenchItem, ICustomDamageItem {
    @Dependency(mod = "ebwizardry:mana")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new Cost().read(json);
        }

        @Override
        public AStaffItem make(StaffEntry entry) {
            return new WizardryManaStaffItem(entry, new Properties());
        }
    }

    public static class Cost extends ValueCost {
        protected int maxUpgrades;

        @Override
        public Cost read(JsonObject json) {
            super.read(json);
            this.maxUpgrades = GsonHelper.getAsInt(json, "max_upgrades", 1);
            return this;
        }

        public int getMaxUpgrades() {
            return this.maxUpgrades;
        }
    }

    protected final Cost cost;

    public WizardryManaStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (Cost) this.getEntry().getCost();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return this.getMana(stack) >= this.getCost(stack, player, cost.getValue());
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        this.consumeMana(stack, this.getCost(stack, player, cost.getValue()), player);
    }

    @Override
    public boolean canMelee(ItemStack stack) {
        return super.canMelee(stack) || CastItemDataHelper.getUpgradeLevel(stack, EBItems.MELEE_UPGRADE.get()) > 0;
    }

    @Override
    public void appendExtraTooltip(ItemStack stack, Player player, List<Component> tooltip) {
        if (this.getCost(stack, player, cost.getValue()) > 0) {
            tooltip.add(Component.translatable("sortilege.staff.cost.mana", this.getCost(stack, player, cost.getValue()))
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.empty());
        }
    }

    @Override
    public int getRange(ItemStack stack) {
        return super.getRange(stack) + CastItemDataHelper.getUpgradeLevel(stack, EBItems.RANGE_UPGRADE.get());
    }

    @Override
    public int getCooldown(ItemStack stack, Player player) {
        float multiplier = 1 - CastItemDataHelper.getUpgradeLevel(stack, EBItems.COOLDOWN_UPGRADE.get()) * 0.05f;
        return (int) (super.getCooldown(stack, player) * Math.max(0, multiplier));
    }

    @Override
    public void triggerBlastAttack(ItemStack stack, LivingEntity player, Set<ElementalStaffEnchantment> elements,
                                   Vec3 direction, double x, double y, double z, double radius, List<LivingEntity> targetsHit) {
        super.triggerBlastAttack(stack, player, elements, direction, x, y, z,
                radius + CastItemDataHelper.getUpgradeLevel(stack, EBItems.MELEE_UPGRADE.get()), targetsHit);
    }

    @Override
    public void shoot(ItemStack stack, Player player, Set<ElementalStaffEnchantment> elements, List<float[]> colors,
                      Vec3 direction, List<LivingEntity> targetsHit) {
        super.shoot(stack, player, elements, colors, direction, targetsHit);

        for (LivingEntity entity : targetsHit) {
            if (entity.isDeadOrDying()) {
                this.rechargeMana(stack, EBServerConfig.SIPHON_MANA_PER_LEVEL.get()
                        * CastItemDataHelper.getUpgradeLevel(stack, EBItems.SIPHON_UPGRADE.get()));
            }
        }
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
        if (!world.isClientSide() && !this.isManaFull(stack) && world.getGameTime() % (long) EBServerConfig.CONDENSER_TICK_INTERVAL.get() == 0)
            this.rechargeMana(stack, CastItemDataHelper.getUpgradeLevel(stack, EBItems.CONDENSER_UPGRADE.get()));
    }

    @Override
    public int getCustomMaxDamage(ItemStack stack) {
        return (int) (this.getMaxDamage() * (1 + EBServerConfig.STORAGE_INCREASE_PER_LEVEL.get()
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

        if (CastItemDataHelper.getTotalUpgrades(stack) < this.cost.getMaxUpgrades()
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
