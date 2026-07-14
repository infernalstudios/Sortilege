package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.item.custom.AStaffItem;
import net.lyof.sortilege.item.staff.IStaffEntryReader;
import net.lyof.sortilege.item.staff.StaffEntry;
import net.lyof.sortilege.item.staff.entry.ValueCost;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class HealthStaffItem extends AStaffItem {
    @Dependency(mod = "sortilege:health")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new Cost().read(json);
        }

        @Override
        public void register(StaffEntry entry, BiConsumer<String, AStaffItem> registrar) {
            registrar.accept(entry.getID(), new HealthStaffItem(entry, new Properties()));
        }
    }

    public static class Cost extends ValueCost {
        protected boolean freeOnMiss;
        protected boolean freeOnKill;

        @Override
        public Cost read(JsonObject json) {
            super.read(json);
            this.freeOnMiss = GsonHelper.getAsBoolean(json, "free_on_miss", true);
            this.freeOnKill = GsonHelper.getAsBoolean(json, "free_on_kill", true);
            return this;
        }

        public boolean isFreeOnMiss() {
            return this.freeOnMiss;
        }

        public boolean isFreeOnKill() {
            return this.freeOnKill;
        }
    }

    private static final Set<ItemStack> toConsume = new HashSet<>();

    protected final Cost cost;

    public HealthStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (Cost) this.getEntry().getCost();
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return player.getHealth() > this.getCost(stack, player, cost.getValue());
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        if (cost.isFreeOnMiss())
            toConsume.add(stack);
        else
            this.consumeHealth(stack, player);
    }

    public void consumeHealth(ItemStack stack, Player player) {
        player.hurt(player.damageSources().wither(), this.getCost(stack, player, cost.getValue()));
    }

    @Override
    public void onKill(ItemStack stack, LivingEntity player, LivingEntity target) {
        super.onKill(stack, player, target);

        if (cost.isFreeOnKill())
            toConsume.remove(stack);
    }

    @Override
    public void onHit(ItemStack stack, LivingEntity player, LivingEntity target) {
        super.onHit(stack, player, target);

        if (toConsume.contains(stack) && player instanceof Player p) {
            toConsume.remove(stack);
            this.consumeHealth(stack, p);
        }
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getValue()) > 0)
            tooltip.add(Component.translatable("tooltip.sortilege.staff.cost.health", this.getCost(stack, player, cost.getValue())).withStyle(ChatFormatting.DARK_RED));
    }
}
