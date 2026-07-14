package net.lyof.sortilege.item.custom.staff;

import com.google.gson.JsonObject;
import dev.onyxstudios.cca.api.v3.component.ComponentAccess;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.ChunkAethumComponent;
import io.wispforest.affinity.component.PlayerAethumComponent;
import net.lcc.sollib.platform.Dependency;
import net.lyof.sortilege.enchant.staff.ElementalStaffEnchantment;
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

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class AffinityStaffItem extends AStaffItem {
    @Dependency(mod = "affinity:aethum")
    public static class Reader implements IStaffEntryReader {
        @Override
        public StaffEntry.Cost readCost(JsonObject json) {
            return new ValueCost().read(json);
        }

        @Override
        public void register(StaffEntry entry, BiConsumer<String, AStaffItem> registrar) {
            registrar.accept(entry.getID(), new AffinityStaffItem(entry, new Properties()));
        }
    }

    public static class Effects extends StaffEntry.Effects {
        protected float aethumDamageMultiplier;

        @Override
        public Effects read(JsonObject json) {
            super.read(json);
            this.aethumDamageMultiplier = GsonHelper.getAsFloat(json, "aethum_damage_multiplier", 0.25f);
            return this;
        }

        public float getAethumDamageMultiplier() {
            return this.aethumDamageMultiplier;
        }
    }

    protected final ValueCost cost;
    protected final Effects effects;

    public AffinityStaffItem(StaffEntry entry, Properties properties) {
        super(entry, properties);
        this.cost = (ValueCost) this.getEntry().getCost();
        this.effects = (Effects) this.getEntry().getEffects();
    }

    @Override
    public float modifyDamageDealt(ItemStack stack, float damage, LivingEntity player, LivingEntity target, Set<ElementalStaffEnchantment> elements) {
        float o = super.modifyDamageDealt(stack, damage, player, target, elements);
        if (effects.getAethumDamageMultiplier() == 0 || !(target.level().getChunk(target.blockPosition()) instanceof ComponentAccess access))
            return o;

        ChunkAethumComponent aethum = access.getComponent(AffinityComponents.CHUNK_AETHUM);
        float m = 1 + ((float) aethum.getAethum() - 50) * 0.02f * effects.getAethumDamageMultiplier();

        return o*m;
    }

    @Override
    public boolean hasResource(ItemStack stack, Player player) {
        return player instanceof ComponentAccess access &&
                access.getComponent(AffinityComponents.PLAYER_AETHUM).hasAethum(this.getCost(stack, player, cost.getValue()));
    }

    @Override
    public void consumeResource(ItemStack stack, Player player) {
        if (player instanceof ComponentAccess access) {
            PlayerAethumComponent aethum = access.getComponent(AffinityComponents.PLAYER_AETHUM);
            aethum.addAethum(-this.getCost(stack, player, cost.getValue()));
        }
    }

    @Override
    public void appendTooltipAbilities(ItemStack stack, Player player, List<Component> tooltip) {
        if (effects.getAethumDamageMultiplier() != 0)
            tooltip.add(Component.translatable("tooltip.sortilege.staff.affinity").withStyle(ChatFormatting.GRAY));

        super.appendTooltipAbilities(stack, player, tooltip);
    }

    @Override
    public void appendTooltipCosts(ItemStack stack, Player player, List<Component> tooltip) {
        super.appendTooltipCosts(stack, player, tooltip);

        if (this.getCost(stack, player, cost.getValue()) > 0)
            tooltip.add(Component.translatable("item.affinity.staff.tooltip.consumption_per_use", this.getCost(stack, player, cost.getValue())).withStyle(ChatFormatting.GREEN));
    }
}
